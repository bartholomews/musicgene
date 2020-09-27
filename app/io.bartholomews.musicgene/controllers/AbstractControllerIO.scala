package io.bartholomews.musicgene.controllers

import cats.effect.IO
import io.bartholomews.musicgene.controllers.http.SessionKeys
import play.api.mvc._

abstract class AbstractControllerIO(override protected val controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  self =>

  object ActionIO {
    // FIXME: This is spotify-specific
    final def asyncWithSession(sessionNumber: Int)(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(req => block(req.addAttr(SessionKeys.spotifySessionKey, sessionNumber)).unsafeToFuture())

    final def asyncWithDefaultUser(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(req => block(req.addAttr(SessionKeys.spotifySessionKey, 0)).unsafeToFuture())

    final def async(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(block.andThen(_.unsafeToFuture()))

    final def async(block: => IO[Result]): Action[AnyContent] =
      self.Action.async(block.unsafeToFuture())

    final def async[A](bodyParser: BodyParser[A])(block: Request[A] => IO[Result]): Action[A] =
      self.Action.async(bodyParser)(block.andThen(_.unsafeToFuture()))
  }

}
