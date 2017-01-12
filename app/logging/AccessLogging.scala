package logging

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

/**
  * https://www.playframework.com/documentation/2.5.x/ScalaLogging
  */
trait AccessLogging {

  // @see https://www.playframework.com/documentation/2.5.x/SettingsLogger
  val accessLogger = Logger("application")

  def withLogger(call: Future[WSResponse])(action: WSResponse => Result): Future[Result] =
    call map { response: WSResponse => {
      accessLogger.debug(response.body)
      action(response)
    }}

}
