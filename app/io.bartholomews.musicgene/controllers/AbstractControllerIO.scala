package io.bartholomews.musicgene.controllers

import cats.effect.IO
import io.bartholomews.musicgene.controllers.http.SpotifySessionKeys
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import play.api.libs.json.JsValue
import play.api.mvc._

abstract class AbstractControllerIO(override protected val controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  self =>

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
