package controllers

import cats.effect.IO
import play.api.mvc._

abstract class AbstractControllerIO(protected override val controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) {

  self =>

  object ActionIO {
    final def async(block: Request[AnyContent] => IO[Result]): Action[AnyContent] =
      self.Action.async(block.andThen(_.unsafeToFuture()))

    final def async(block: => IO[Result]): Action[AnyContent] =
      self.Action.async(block.unsafeToFuture())

    final def async[A](bodyParser: BodyParser[A])(block: Request[A] => IO[Result]): Action[A] =
      self.Action.async(bodyParser)(block.andThen(_.unsafeToFuture()))
  }

}
