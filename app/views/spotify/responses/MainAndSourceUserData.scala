package views.spotify.responses

import io.bartholomews.spotify4s.core.entities.PrivateUser
import play.api.libs.json.JsError
import sttp.client3.ResponseException

final case class MainAndSourceUserData[T](main: UserDataResponse[T], source: Option[UserDataResponse[T]])
final case class UserDataResponse[T](user: PrivateUser, response: Either[ResponseException[String, JsError], T])
