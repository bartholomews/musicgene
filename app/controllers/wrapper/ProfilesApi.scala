package controllers.wrapper

import javax.inject.Inject

import logging.AccessLogging
import controllers.wrapper.entities.UserPrivate
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * @see https://developer.spotify.com/web-api/user-profile-endpoints/
  */
class ProfilesApi @Inject()(configuration: play.api.Configuration, ws: WSClient,
                            api: BaseApi) extends AccessLogging {

  private final val ME = s"${api.BASE_URL}/me"
  private final def USERS(id: Long) = s"${api.BASE_URL}/users/$id"

  def me: Future[UserPrivate] = api.getWithOAuth[UserPrivate](ME)

}
