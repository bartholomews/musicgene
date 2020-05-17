package controllers.http

import java.time.Clock

import io.bartholomews.fsclient.entities.oauth.AuthorizationCode
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.RefreshToken
import pdi.jwt.JwtSession
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{AnyContent, Cookie, Request, Result}

case object SpotifyCookies {
  final val accessCookieName: String = "spotify4s_access_cookie"
  final val refreshCookieName: String = "spotify4s_refresh_cookie"

  // Todo S <: SignerV2
  def getAccessCookieCredentials(request: Request[AnyContent]): Option[AuthorizationCode] = {
    import JsonProtocol.authorizationTokenReads
    JwtSessionCookie.extractCookie(accessCookieName, request)
  }

  def getRefreshCookieCredentials(request: Request[AnyContent]): Option[RefreshToken] = {
    import JsonProtocol.refreshTokenFormat
    JwtSessionCookie.extractCookie[RefreshToken](refreshCookieName, request)
  }

  def serializeCookieCredentials(accessToken: AuthorizationCode): List[Cookie] = {
    import JsonProtocol.{authorizationTokenWrites, refreshTokenFormat}
    JwtSessionCookie.serializeCookie(accessCookieName, accessToken) +:
      accessToken.refreshToken.toList.map(rt => JwtSessionCookie.serializeCookie(refreshCookieName, rt))
  }
}

case object DiscogsCookies {
  final val accessCookieName: String = "discogs4s_access_cookie"

  def getCookieCredentials[A](request: Request[AnyContent])(implicit reads: Reads[A]): Option[A] =
    JwtSessionCookie.extractCookie[A](accessCookieName, request)

  def serializeCookieCredentials[A](token: A)(implicit writes: Writes[A]): Cookie =
    JwtSessionCookie.serializeCookie[A](accessCookieName, token)

  def clearCookies(request: Result) = request.withNewSession
}

object JwtSessionCookie {

  implicit val clock: Clock = Clock.systemUTC
  implicit val conf: Configuration = Configuration.reference

  private val cookieFieldName = "token"

  def extractCookie[A](cookieObjectName: String, request: Request[AnyContent])(
    implicit reads: Reads[A]
  ): Option[A] =
    request.cookies
      .get(cookieObjectName)
      .flatMap { maybeCookie =>
        JwtSession.deserialize(maybeCookie.value).getAs[A](cookieFieldName)
      }

  def serializeCookie[A](cookieObjectName: String, obj: A)(
    implicit writes: Writes[A]
  ): Cookie =
    Cookie(
      name = cookieObjectName,
      value = (JwtSession() + (cookieFieldName, obj)).serialize
      // TODO: secure = true in env.prod
    )
}
