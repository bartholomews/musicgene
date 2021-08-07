package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import com.google.inject.Inject
import eu.timepit.refined.api.Refined
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.{AccessTokenSigner, SignerV2}
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.http.{SpotifyCookies, SpotifySessionKeys}
import io.bartholomews.musicgene.model.genetic.GA
import io.bartholomews.musicgene.model.music._
import io.bartholomews.musicgene.model.spotify.SpotifyService
import io.bartholomews.spotify4s.core.api.SpotifyApi.{Offset, SpotifyUris}
import io.bartholomews.spotify4s.core.entities.SpotifyId.{SpotifyPlaylistId, SpotifyUserId}
import io.bartholomews.spotify4s.core.entities._
import play.api.Logging
import play.api.libs.json.{JsError, JsResult, JsValue, Json}
import play.api.mvc._
import sttp.client3.{Response, ResponseException, SttpBackend}
import sttp.model.Uri
import views.common.Tab
import views.spotify.requests.{
  PlaylistGenerationRequest,
  PlaylistMigrationRequest,
  PlaylistsMigrationRequest,
  PlaylistsUnfollowRequest
}
import views.spotify.responses._

import javax.inject._
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 */
@Singleton
class SpotifyController @Inject() (cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
    with Logging
    with play.api.i18n.I18nSupport {

  import cats.implicits._
  import eu.timepit.refined.auto.autoRefineV
  import io.bartholomews.musicgene.controllers.http.SpotifyHttpResults._
  import io.bartholomews.musicgene.model.helpers.CollectionsHelpers._
  import io.bartholomews.spotify4s.playJson.codecs._
  import sttp.client3.playJson._

  type DE = JsError
  type F[X] = Future[X]

  object SpotifyAction {
    final def asyncWithSession(
      session: SpotifySessionUser
    )(block: Request[AnyContent] => F[Result]): Action[AnyContent] =
      Action.async(req => block(req.addAttr(SpotifySessionKeys.spotifySessionUser, session)))

    final def asyncWithMainUser(block: Request[AnyContent] => F[Result]): Action[AnyContent] =
      Action.async(req => block(req.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main)))

    final def jsonAsyncWithMainUser(block: Request[JsValue] => F[Result]): Action[JsValue] =
      Action.async[JsValue](parse.json)(req =>
        block(req.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main))
      )
  }

  import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
  val spotifyBackend: SttpBackend[F, Any] = AsyncHttpClientFutureBackend()

  val spotifyService: SpotifyService[F] = new SpotifyService(spotifyBackend)

  def authenticate(session: SpotifySessionUser): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Authenticating session $session")
    Future(authenticate(request.addAttr(SpotifySessionKeys.spotifySessionUser, session)))
  }

  /**
   * Redirect a user to authenticate with Spotify and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(implicit request: Request[AnyContent]): Result = {
    val maybeSession = request.attrs.get(SpotifySessionKeys.spotifySessionUser)
    logger.info(s"Authenticate request session: $maybeSession")
    maybeSession
      .map { session: SpotifySessionUser =>
        println(session.entryName)
        redirect(spotifyService.authorizeUrl(session))
      }
      .getOrElse(InternalServerError("Something went wrong handling spotify session, please contact support."))
  }

  def helloPage(): Action[AnyContent] = SpotifyAction.asyncWithMainUser { implicit request =>
    withToken { signer =>
      logger.info("hello")
      spotifyService
        .me(signer)
        .map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback(session: SpotifySessionUser): Action[AnyContent] = SpotifyAction.asyncWithSession(session) {
    implicit request =>
      (for {
        _ <- EitherT.pure[F, String](logger.debug(s"Callback for session: $session"))

        query = request.rawQueryString
        redirectionUriResponse <- EitherT.fromEither[F](
          Uri.parse(s"${routes.SpotifyController.callback(session).absoluteURL()}?$query")
        )

        authorizationCode <- EitherT(
          spotifyService.client.auth.AuthorizationCode
          // FIXME: find a way to let infer the E type and work on mapping response
            .acquire[DE](spotifyService.userAuthorizationRequest(session), redirectionUriResponse)
            .map(_.body.leftMap(_.getMessage))
        )
      } yield Redirect(
        session match {
          case SpotifySessionUser.Main   => routes.SpotifyController.helloPage()
          case SpotifySessionUser.Source => routes.SpotifyController.migratePage()
        }
      ).withCookies(SpotifyCookies.accessCookies(authorizationCode): _*)).value.map(_.fold(errorString => {
        logger.error(errorString)
        Redirect(routes.HomeController.index())
      }, identity))
  }

  def logout(session: SpotifySessionUser): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(s"Logout session $session")
    Future(session match {
      case SpotifySessionUser.Main =>
        Redirect(routes.HomeController.index())
          .discardingCookies(SpotifyCookies.discardAllCookies: _*)
      case SpotifySessionUser.Source =>
        Redirect(routes.SpotifyController.migratePage())
          .discardingCookies(SpotifyCookies.discardCookies(session): _*)
    })
  }

  private def refresh(f: SignerV2 => F[Result])(implicit request: Request[AnyContent]): F[Result] =
    SpotifyCookies
      .extractRefreshToken(request)
      .fold(Future.successful(authenticate(request)))(token =>
        spotifyService.client.auth.AuthorizationCode
          .refresh[DE](token)
          .flatMap(_.toResultF[F] { authorizationCode =>
            f(authorizationCode).map(
              _.withCookies(
                SpotifyCookies.accessCookies(authorizationCode): _*
              )
            )
          })
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](f: SignerV2 => F[Result])(implicit request: Request[AnyContent]): F[Result] =
    SpotifyCookies.extractAuthCode(request) match {
      case None => Future.successful(authenticate(request))
      case Some(accessToken: AccessTokenSigner) =>
        logger.debug(
          s"""
          |spotify token:
          |generated_at => ${accessToken.generatedAt}
          |expires_in => ${accessToken.expiresIn} (${accessToken.expiresIn * 1000})
          |system.currentTimeMillis => ${System.currentTimeMillis()}
          |token_duration => ${accessToken.generatedAt + (accessToken.expiresIn * 1000)}
          |is_expired => ${accessToken.isExpired()}
          |"""
        )
        if (accessToken.isExpired()) refresh(f)
        else f(accessToken)
    }

  def withSourceUserToken[A, R](f: SignerV2 => F[Result])(implicit request: Request[AnyContent]): F[Option[Result]] =
    SpotifyCookies.extractAuthCode(SpotifySessionUser.Source) match {
      case None => Future.successful(None)
      case Some(accessToken: AccessTokenSigner) =>
        if (accessToken.isExpired())
          refresh(f)(request.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Source))
            .map(_.some)
        else f(accessToken).map(_.some)
    }

  /**
   * @return the FIRST PAGE of a user playlists TODO
   */
  def playlists(session: SpotifySessionUser, page: Int): Action[AnyContent] =
    SpotifyAction.asyncWithSession(session) { implicit request =>
      withToken { accessToken =>
        val pageLimit: SimplePlaylist.Limit = 50
        // FIXME - move page calc in spotify4s
        val pageOffset: Offset = Refined.unsafeApply((page - 1) * pageLimit.value)
        spotifyService.client.users
          .getPlaylists(limit = pageLimit, offset = pageOffset)(accessToken)
          .map(_.toResult(pg => Ok(views.html.spotify.playlist_generation.playlists("Playlists", pg.items, page))))
      }
    }

  def tracks(session: SpotifySessionUser, playlistId: SpotifyId): Action[AnyContent] =
    SpotifyAction.asyncWithSession(session) { implicit request =>
      withToken { accessToken =>
        spotifyService.client.playlists
          .getPlaylist[DE](playlistId)(accessToken)
          .map(_.toResult { playlist =>
            val tracks: List[FullTrack] = playlist.tracks.items.map(_.track)
            Ok(views.html.spotify.playlist_generation.tracks(playlist.tracks.copy(items = tracks)))
          })
      }
    }

  // FIXME: Add sessionNumber to request
  val generatePlaylist: Action[JsValue] = Action.async[JsValue](parse.json) { implicit request =>
    val playlistRequestJson = request.body.validate[PlaylistGenerationRequest]
    playlistRequestJson.fold(
      errors =>
        Future.successful(
          BadRequest(
            Json.obj(
              "error" -> "invalid_playlist_request_payload",
              "message" -> JsError.toJson(errors)
            )
          )
        ),
      playlistRequest =>
        // val db = getFromRedisThenMongo(p)
        //        Ok(Json.toJson(PlaylistResponse.fromPlaylist(p)))
        generatePlaylist(playlistRequest)(
          request
            .addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main)
            .map(AnyContentAsJson)
        )
    )
  }

  private def generatePlaylist(
    playlistRequest: PlaylistGenerationRequest
  )(implicit request: Request[AnyContent]): F[Result] =
    withToken { implicit accessToken =>
      val getTracks: F[Either[Result, List[FullTrack]]] =
        playlistRequest.tracks
          .groupedNes(size = 50)(SpotifyId.order.toOrdering)
          .map(xs =>
            spotifyService.client.tracks
              .getTracks[DE](xs, market = None)(accessToken)
              .map(_.body.leftMap(errorToJsonResult))
          )
          //          .parSequence
          .sequence
          .map(_.sequence.map(_.flatten))

      val getAudioFeatures: F[Either[Result, List[AudioFeatures]]] =
        playlistRequest.tracks
          .groupedNes(size = 100)(SpotifyId.order.toOrdering)
          .map(xs =>
            spotifyService.client.tracks
              .getAudioFeatures[DE](xs)(accessToken)
              .map(_.body.leftMap(errorToJsonResult))
          )
          //          .parSequence
          .sequence
          .map(_.sequence.map(_.flatten))

      Tuple2(getTracks, getAudioFeatures)
        .mapN({
          // .parMapN({
          case (getTracksResult, getAudioFeaturesResult) =>
            (for {
              // MongoController.writeToDB(dbTracks, song) // TODO only if not already there
              audioFeaturesLookup <- getAudioFeaturesResult
                .map(af => af.map(f => Tuple2(f.id, f)).toMap)
              spotifyTracks <- getTracksResult
              audioTracks = spotifyTracks.map { track =>
                track.id.fold(AudioTrack(track, None)) { trackId =>
                  audioFeaturesLookup
                    .get(trackId)
                    .fold(AudioTrack(track, None))(af => AudioTrack(track, Some(af)))
                }
              }
              playlist = GA.generatePlaylist(
                db = new MusicCollection(audioTracks),
                c = playlistRequest.constraints.map(_.toDomain),
                playlistRequest.size
              )
            } yield Ok(Json.toJson(GeneratedPlaylist.fromPlaylist(playlistRequest.name, playlist))))
              .fold(identity, identity)
        })
    }

  def renderGeneratedPlaylist(generatedPlaylistResultId: GeneratedPlaylistResultId): Action[AnyContent] =
    Action.async { implicit request =>
      // TODO could store previously generated playlist results
      Future.successful(
        Ok(views.html.spotify.playlist_generation.playlistGeneration(generatedPlaylistResultId, List.empty))
      )
    }

  private def getSourceUserForMigration(
    mainUser: (PrivateUser, Page[SimplePlaylist])
  )(implicit request: Request[AnyContent]): F[Result] =
    withSourceUserToken { implicit signer =>
      logger.info("getSourceUserForMigration")
      spotifyService.getUserAndPlaylists.map(
        _.toResulto(entity =>
          Ok(views.html.spotify.migrate.playlists.migratePlaylists(Right(Tuple2(mainUser, Some(entity)))))
        )
      )
    }.map(_.getOrElse(Ok(views.html.spotify.migrate.playlists.migratePlaylists(Right(Tuple2(mainUser, None))))))

  private def getMainUserAndPlaylists(
    andThen: ((PrivateUser, Page[SimplePlaylist])) => F[Result]
  )(implicit request: Request[AnyContent]): F[Result] =
    withToken { implicit signer =>
      spotifyService.getUserAndPlaylists.flatMap(
        _.fold(
          _ =>
            Ok(
              views.html.spotify.migrate.playlists
                .migratePlaylists(Left("Something went wrong while fetching the main user"))
            ).pure[F],
          entity => andThen(entity)
        )
      )
    }

  private def withMainAndSourceUsers(
    fa: PrivateUser => Result,
    fb: (PrivateUser, Some[PrivateUser]) => Result
  )(implicit request: Request[AnyContent]): F[Result] = withToken { main =>
      spotifyService
        .getUser(main)
        .flatMap(
          _.fold(
            error => Future.successful(BadRequest(views.html.common.error(error.getMessage, Tab.Spotify))),
            mainUser =>
              withSourceUserToken { src =>
                spotifyService.getUser(src).map(_.fold(_ => fa(mainUser), srcUser => fb(mainUser, Some(srcUser))))
              }.map(_.getOrElse(fa(mainUser)))
          )
        )
    }

  def migratePage(): Action[AnyContent] = SpotifyAction.asyncWithMainUser { implicit request =>
    withMainAndSourceUsers(
      mainUser => Ok(views.html.spotify.migrate.migratePage(mainUser, None)),
      (mainUser, srcUser) => Ok(views.html.spotify.migrate.migratePage(mainUser, srcUser))
    )
  }

  def migrateFollowersPage(): Action[AnyContent] = SpotifyAction.asyncWithMainUser { implicit request =>
    withMainAndSourceUsers(
      mainUser => Ok(views.html.spotify.migrate.followers.migrateFollowers(mainUser, None)),
      (mainUser, srcUser) => Ok(views.html.spotify.migrate.followers.migrateFollowers(mainUser, srcUser))
    )
  }

  def migratePlaylistsPage(): Action[AnyContent] = SpotifyAction.asyncWithMainUser { implicit request =>
    withToken { implicit signer =>
      logger.info("migratePlaylistsPage")
      getMainUserAndPlaylists(getSourceUserForMigration)
    }
  }

  def addTracksToPlaylist[E](
    playlistResult: SttpResponse[E, FullPlaylist],
    tracks: SpotifyUris
  )(implicit request: Request[AnyContent], signer: SignerV2): F[Either[String, Result]] =
    //    playlistResult.toResultEither[F] { playlist =>
    playlistResult.body.fold(
      error => Future.successful(Left(error.getMessage)),
      playlist =>
        spotifyService.client.playlists
          .addTracksToPlaylist[DE](
            playlistId = playlist.id,
            uris = tracks, // FIXME: Need to get ALL pages...
            position = None
          )(signer)
          .map(
            _.body.bimap(
              _.getMessage, // FIXME
              _ => Ok("Playlist have been migrated.")
            )
          )
    )

  //    }

  private def clonePlaylist(
    mainUserId: SpotifyUserId,
    playlistRequest: PlaylistMigrationRequest,
    playlistToClone: FullPlaylist
  )(implicit request: Request[AnyContent], signer: SignerV2): F[Either[String, Result]] =
    spotifyService.client.playlists
      .createPlaylist(
        userId = mainUserId,
        playlistName = playlistRequest.name,
        public = playlistRequest.public,
        collaborative = playlistRequest.collaborative,
        description = playlistRequest.description
      )(signer)
      .flatMap { (newPlaylistResponse: SttpResponse[DE, FullPlaylist]) =>
        SpotifyUri
          .fromList(
            playlistToClone.tracks.items
              .filterNot(_.isLocal) // FIXME: :( maybe should add to the response to inform the user
              .map(_.track.uri)
          )
          .fold(
            error => Future.successful(Left(error)),
            uris => addTracksToPlaylist(newPlaylistResponse, uris)
          )
      }

  def unfollowPlaylistsPPP(mainUserId: SpotifyUserId, playlistsToUnfollow: List[SpotifyPlaylistId])(
    implicit request: Request[AnyContent]
  ): F[Result] =
    withToken { implicit token =>
      playlistsToUnfollow
        .map(id => spotifyService.client.follow.unfollowPlaylist(id)(token))
        //        .parSequence
        .sequence
        .map { re =>
          val (a, b) = re.partition(_.isSuccess)
          Ok(
            Json.obj(
              ("success", a.size),
              ("failures", b.size)
            )
          )
        }
    }

  // fields = "tracks(total,previous,next,limit,offset,items(track(id)))"
  // FIXME: Generic and add to spotify4s
  def getAllOffsetToFetch[A](page: Page[A], limit: Int = 100)(implicit signer: SignerV2): List[Int] = {
    @tailrec
    def offsets(curr: Int, acc: List[Int]): List[Int] =
      if (curr >= page.total) acc
      else offsets(curr + limit, curr :: acc)

    offsets(page.items.length, Nil)
  }

  private def migratePlaylist(
    mainUserId: SpotifyUserId,
    playlistRequest: PlaylistMigrationRequest,
    mainUserToken: SignerV2,
    sourceUserToken: SignerV2
  )(implicit request: Request[AnyContent]): F[Either[String, Result]] =
    (for {
      // FIXME: Should probably use getPlaylistFields(/tracks)
      sourceUserPlaylist <- EitherT(
        spotifyService.client.playlists
          .getPlaylist[DE](playlistId = playlistRequest.id, market = None)(sourceUserToken)
          .map(_.body.leftMap(_.getMessage))
      )

      res <- EitherT(clonePlaylist(mainUserId, playlistRequest, sourceUserPlaylist)(request, mainUserToken))
    } yield res).value

  val unfollowPlaylists: Action[JsValue] = SpotifyAction.jsonAsyncWithMainUser { implicit request =>
    val unfollowPlaylistsRequest = request.body.validate[PlaylistsUnfollowRequest]
    unfollowPlaylistsRequest.fold(
      errors =>
        Future.successful(
          BadRequest(
            Json.obj(
              "error" -> "invalid_playlist_request_payload",
              "message" -> JsError.toJson(errors)
            )
          )
        ),
      playlistRequests => {
        logger.debug(s"user => ${playlistRequests.userId.value}")
        playlistRequests.playlists.foreach { pr =>
          logger.debug(pr.toString)
        }

        unfollowPlaylistsPPP(playlistRequests.userId, playlistRequests.playlists.map(_.id))(
          request.map(AnyContent.apply)
        )
      }
    )
  }

  def migratePlaylists(playlistRequests: PlaylistsMigrationRequest, mainUserToken: SignerV2)(
    implicit request: Request[JsValue]
  ) =
    withSourceUserToken { sourceUserToken =>
      logger.debug(s"user => ${playlistRequests.userId.toString}")
      playlistRequests.playlists.foreach { pr =>
        logger.debug(pr.toString)
      }

      playlistRequests.playlists
        .map { p =>
          migratePlaylist(
            playlistRequests.userId,
            p,
            mainUserToken,
            sourceUserToken
          )(request.map(AnyContent.apply))
        }
        .sequence
        .map { re =>
          // Ior?
          val (a, b) = re.partition(result => result.isRight)
          Ok(
            Json.obj(
              ("success", a.size),
              ("failures", b.size)
            )
          )
        }
    }(request.map(AnyContent.apply))

  // Would be nice to give feedback while loading spinner, e.g. "Migrating playlist xxx..."
  // https://www.playframework.com/documentation/2.8.x/ScalaWebSockets
  val migratePlaylists: Action[JsValue] = SpotifyAction.jsonAsyncWithMainUser { implicit request: Request[JsValue] =>
    implicit val anyContentRequest: Request[AnyContent] = request.map(AnyContent.apply)
    val playlistIdsRequest: JsResult[PlaylistsMigrationRequest] = request.body.validate[PlaylistsMigrationRequest]
    playlistIdsRequest.fold(
      errors =>
        Future.successful(
          BadRequest(
            Json.obj(
              "error" -> "invalid_playlist_request_payload",
              "message" -> JsError.toJson(errors)
            )
          )
        ),
      playlistRequests =>
        withToken(mainUserToken =>
          migratePlaylists(playlistRequests, mainUserToken).map(
            _.getOrElse(BadRequest("Empty WAT ?"))
          )
        )
    )
  }
}

// TODO:
//  -> Remove duplicates (two playlists with same name and exactly same tracks)
//  -> Remove all playlists (should do a DANGER modal)
//  -> Websocket or something like that to provide incremental feedback and partial failures
//  -> Also refresh playlists rows
//  -> Get all pages
