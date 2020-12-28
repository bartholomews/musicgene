package io.bartholomews.musicgene.controllers

import cats.effect.{ContextShift, IO}
import io.bartholomews.musicgene.controllers.http.SpotifySessionKeys
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClient}
import play.api.libs.json.JsValue
import play.api.mvc._
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler

import scala.concurrent.ExecutionContext

abstract class AbstractControllerIO(override protected val controllerComponents: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractController(controllerComponents) {

  self =>

  import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  // https://sttp.softwaremill.com/en/stable/backends/catseffect.html
  val asyncHttpClient: AsyncHttpClient = new DefaultAsyncHttpClient()
  implicit val backend: SttpBackend[IO, Nothing, WebSocketHandler] =
    AsyncHttpClientCatsBackend.usingClient[IO](asyncHttpClient)

  object ActionIO {
    // FIXME: This is spotify-specific
    final def asyncWithSession(
      session: SpotifySessionUser
    )(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(req => block(req.addAttr(SpotifySessionKeys.spotifySessionUser, session)).unsafeToFuture())

    // FIXME: This is spotify-specific
    final def asyncWithMainUser(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(req =>
        block(req.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main)).unsafeToFuture()
      )

    // FIXME: This is spotify-specific
    final def jsonAsyncWithMainUser(block: Request[JsValue] => IO[Result]): Action[JsValue] =
      self.Action.async[JsValue](parse.json)(req =>
        block(req.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main)).unsafeToFuture()
      )

    final def async(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(block.andThen(_.unsafeToFuture()))

    final def async(block: => IO[Result]): Action[AnyContent] =
      self.Action.async(block.unsafeToFuture())

    final def async[A](bodyParser: BodyParser[A])(block: Request[A] => IO[Result]): Action[A] =
      self.Action.async(bodyParser)(block.andThen(_.unsafeToFuture()))
  }

}
