package controllers.http

import java.time.Clock

import io.bartholomews.fsclient.entities.oauth.AuthorizationCode
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.RefreshToken
import pdi.jwt.JwtSession
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}
import play.api.mvc._

case object SpotifySession {
  final val accessSessionKey: String = "spotify4s_access_session"
  final val refreshSessionKey: String = "spotify4s_refresh_session"

  def serializeSession(accessToken: AuthorizationCode): List[(String, String)] = {
    import JsonProtocol.{authorizationTokenWrites, refreshTokenFormat}
    JwtSessionCookie.withSessionCredentials(accessSessionKey, accessToken) +:
      accessToken.refreshToken.toList
        .map(rt => JwtSessionCookie.withSessionCredentials(refreshSessionKey, rt))
  }

  // Todo S <: SignerV2
  def getAccessSession(request: Request[AnyContent]): Option[AuthorizationCode] = {
    import JsonProtocol.authorizationTokenReads
    JwtSessionCookie.extractSession(accessSessionKey, request)
  }

  def getRefreshSession(request: Request[AnyContent]): Option[RefreshToken] = {
    import JsonProtocol.refreshTokenFormat
    JwtSessionCookie.extractSession[RefreshToken](refreshSessionKey, request)
  }
}

case object DiscogsSession {
  final val sessionKey: String = "discogs4s_session"

  def serializeSession[A](token: A)(implicit writes: Writes[A]): (String, String) =
    JwtSessionCookie.withSessionCredentials[A](sessionKey, token)

  def getSession[A](request: Request[AnyContent])(implicit reads: Reads[A]): Option[A] =
    JwtSessionCookie.extractSession[A](sessionKey, request)
}

object JwtSessionCookie {

  implicit val clock: Clock = Clock.systemUTC
  implicit val conf: Configuration = Configuration.reference

  private val jwtToken = "token"

  def extractSession[A](sessionName: String, request: Request[AnyContent])(
    implicit reads: Reads[A]
  ): Option[A] =
    request.session
      .get(sessionName)
      .flatMap { maybeSessionValue =>
        JwtSession.deserialize(maybeSessionValue).getAs[A](jwtToken)
      }

  def withSessionCredentials[A](session: (String, A))(implicit writes: Writes[A]): (String, String) = session match {
    case (sessionName, obj) => (sessionName, (JwtSession() + (jwtToken, obj)).serialize)
  }
}
