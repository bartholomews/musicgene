package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.google.inject.Inject
import eu.timepit.refined.api.Refined
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, SignerV2}
import io.bartholomews.musicgene.controllers.http.{SessionKeys, SpotifyCookies}
import io.bartholomews.musicgene.model.genetic.GA
import io.bartholomews.musicgene.model.music._
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.api.SpotifyApi.{Limit, Offset}
import io.bartholomews.spotify4s.entities._
import javax.inject._
import org.http4s.Uri
import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import views.spotify.requests.PlaylistRequest
import views.spotify.responses.{GeneratedPlaylist, GeneratedPlaylistResultId}

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class SpotifyController @Inject() (cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractControllerIO(cc)
    with Logging
    with play.api.i18n.I18nSupport {

  import eu.timepit.refined.auto.autoRefineV
  import io.bartholomews.musicgene.controllers.http.SpotifyHttpResults._
  import io.bartholomews.musicgene.model.helpers.CollectionsHelpers._

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val spotifyClient: SpotifyClient = SpotifyClient.unsafeFromConfig()

  def authenticate(sessionNumber: Int): Action[AnyContent] = ActionIO.async { implicit request =>
    logger.info(s"Authenticating session $sessionNumber")
    IO.pure(authenticate(request.addAttr(SessionKeys.spotifySessionKey, sessionNumber)))
  }

  /**
   * Redirect a user to authenticate with Spotify and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(implicit request: Request[AnyContent]): Result = {
    val maybeSessionNumber = request.attrs.get(SessionKeys.spotifySessionKey)
    logger.info(s"Authenticate request session: $maybeSessionNumber")
    maybeSessionNumber
      .map { sessionNumber =>
        redirect(
          spotifyClient.auth.authorizeUrl(
            // TODO: load from config
            redirectUri = Uri.unsafeFromString(s"${requestHost(request)}/spotify/$sessionNumber/callback"),
            state = None,
            scopes = List.empty,
            showDialog = true
          )
        )
      }
      .getOrElse(InternalServerError("Something went wrong handling spotify session, please contact support."))
  }

//  private def getAuthenticatedUsers(sessionNumbers: List[Int], userResponses: List[(HttpResponse[PrivateUser], Int)])(
//    implicit request: Request[AnyContent]
//  ): IO[Result] = {
//    sessionNumbers match {
//      case Nil =>
//        IO.pure {
//          userResponses
//            .foldLeft[IorNel[JsValue, List[(PrivateUser, Int)]]](Ior.Right(List.empty))((result, curr) =>
//              result.combine(
//                curr._1.entity
//                  .bimap(err => NonEmptyList.one(errorToJsValue(err)), user => List(Tuple2(user, curr._2)))
//                  .toIor
//              )
//            )
//            .fold(
//              errors => BadRequest(JsArray.apply(errors.toList)),
//              users => Ok(views.html.spotify.hello(users)),
//              (_, users) => Ok(views.html.spotify.hello(users))
//            )
//        }
//
//      case x :: xs =>
//        withToken { signer =>
//          spotifyClient.users
//            .me(signer)
//            .flatMap(userResponse => {
//              println(s"$x -> ${userResponse.entity.right.get.id.value}")
//              getAuthenticatedUsers(xs, Tuple2(userResponse, x) :: userResponses)
//            })
//        }(request.addAttr(SessionKeys.spotifySessionKey, x))
//    }
//  }

//  def helloAll(): Action[AnyContent] = ActionIO.async { implicit request =>
//    extractAllSessionNumbers.map(_.toInt) match {
//      case Nil      => IO.pure(authenticate(request.addAttr(SessionKeys.spotifySessionKey, 0)))
//      case sessions => getAuthenticatedUsers(sessions, List.empty)
//    }
//  }

  def hello(): Action[AnyContent] = ActionIO.asyncWithDefaultUser { implicit request =>
    withToken { signer =>
      logger.info("hello")
      spotifyClient.users
        .me(signer)
        .map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback(sessionNumber: Int): Action[AnyContent] = ActionIO.asyncWithSession(sessionNumber) { implicit request =>
    (for {
      uri <- EitherT.fromEither[IO](requestUri(request).leftMap(parseFailure => parseFailure.details))
      authorizationCode <- EitherT(
        spotifyClient.auth.AuthorizationCode.fromUri(uri).map(_.entity.leftMap(errorToString))
      )
    } yield Redirect(routes.SpotifyController.hello()).withCookies(
      SpotifyCookies.accessCookies(authorizationCode): _*
    )).value.map(_.fold(errorString => {
      logger.error(errorString)
      Redirect(routes.HomeController.index())
    }, identity))
  }

  def logout(sessionNumber: Int): Action[AnyContent] = ActionIO.asyncWithSession(sessionNumber) { implicit request =>
    logger.debug(s"Logout session $sessionNumber")
    IO.pure(
      Ok(views.html.index())
        .discardingCookies(SpotifyCookies.discardCookies: _*)
    )
  }

  private def refresh(
    f: SignerV2 => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies
      .extractRefreshToken(request)
      .fold(IO.pure(authenticate(request)))(token =>
        spotifyClient.auth.AuthorizationCode
          .refresh(token)
          .flatMap(_.toResultF { authorizationCode =>
            f(authorizationCode).map(
              _.withCookies(
                SpotifyCookies.accessCookies(authorizationCode): _*
              )
            )
          })
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](
    f: SignerV2 => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies.extractAuthCode(request) match {
      case None => IO.pure(authenticate(request))
      case Some(accessToken: AuthorizationCode) =>
        if (accessToken.isExpired()) refresh(f)
        else f(accessToken)
    }

  /**
   * @param userId the Spotify id of the logged-in user
   * @return the FIRST PAGE of a user playlists TODO
   */
  def playlists(sessionNumber: Int, userId: SpotifyUserId, page: Int): Action[AnyContent] =
    ActionIO.asyncWithSession(sessionNumber) { implicit request =>
      withToken { accessToken =>
        val pageLimit: Limit = 50
        val pageOffset: Offset = Refined.unsafeApply((page - 1) * pageLimit.value)
        spotifyClient.playlists
          .getUserPlaylists(
            userId = userId,
            limit = pageLimit,
            offset = pageOffset
          )(accessToken)
          .map(
            _.toResult(pg => Ok(views.html.spotify.playlists("Playlists", pg.items, page)))
          )
      }
    }

  def tracks(sessionNumber: Int, playlistId: SpotifyId): Action[AnyContent] = ActionIO.asyncWithSession(sessionNumber) {
    implicit request =>
      withToken { implicit accessToken =>
        spotifyClient.playlists
          .getPlaylist(playlistId)
          .map(_.toResult { playlist =>
            val tracks: List[FullTrack] = playlist.tracks.items.map(_.track)
            Ok(views.html.spotify.tracks(playlist.tracks.copy(items = tracks)))
          })
      }
  }

  // FIXME: Add sessionNumber to request
  val generatePlaylist: Action[JsValue] = ActionIO.async[JsValue](parse.json) { implicit request =>
    val playlistRequestJson = request.body.validate[PlaylistRequest]
    playlistRequestJson.fold(
      errors =>
        IO.pure(
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
        generatePlaylist(playlistRequest)(request.map(AnyContentAsJson))
    )
  }

  private def generatePlaylist(
    playlistRequest: PlaylistRequest
  )(implicit request: Request[AnyContent]): IO[Result] =
    withToken { implicit accessToken =>
      val getTracks: IO[Either[Result, List[FullTrack]]] =
        playlistRequest.tracks
          .groupedNes(size = 50)
          .map(xs =>
            spotifyClient.tracks
              .getTracks(xs, market = None)
              .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      val getAudioFeatures: IO[Either[Result, List[AudioFeatures]]] =
        playlistRequest.tracks
          .groupedNes(size = 100)
          .map(xs =>
            spotifyClient.tracks
              .getAudioFeatures(xs)
              .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      Tuple2(getTracks, getAudioFeatures).parMapN({
        case (getTracksResult, getAudioFeaturesResult) =>
          (for {
            // MongoController.writeToDB(dbTracks, song) // TODO only if not already there
            audioFeaturesLookup <- getAudioFeaturesResult
              .map(af => af.map(f => Tuple2(f.id, f)).toMap)
            spotifyTracks <- getTracksResult
            audioTracks = spotifyTracks.map { track =>
              track.id.fold(MusicUtil.toAudioTrack(track)) { trackId =>
                audioFeaturesLookup
                  .get(trackId)
                  .fold(MusicUtil.toAudioTrack(track))(af => MusicUtil.toAudioTrack2(track, af))
              }
            }
            playlist = GA.generatePlaylist(
              db = new MusicCollection(audioTracks),
              c = playlistRequest.constraints.map(_.toDomain),
              playlistRequest.length
            )
          } yield Ok(
            Json.toJson(
              GeneratedPlaylist.fromPlaylist(playlistRequest.name, playlist)
            )
          )).fold(identity, identity)
      })
    }

  def renderGeneratedPlaylist(
    generatedPlaylistResultId: GeneratedPlaylistResultId
  ): Action[AnyContent] =
    ActionIO.async { implicit request =>
      // TODO could store previously generated playlist results
      IO.pure(
        Ok(
          views.html.spotify
            .playlist_generation(generatedPlaylistResultId, List.empty)
        )
      )
    }
}
