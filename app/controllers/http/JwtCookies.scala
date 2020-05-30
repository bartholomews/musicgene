package controllers.http

import java.time.Clock

import io.bartholomews.fsclient.entities.oauth.AuthorizationCode
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.RefreshToken
import pdi.jwt.JwtSession
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}
import play.api.mvc._

case object SpotifyCookies {
  final val accessSessionKey: String = "spotify4s_access_session"
  final val refreshSessionKey: String = "spotify4s_refresh_session"

  def accessCookies(accessToken: AuthorizationCode): List[Cookie] = {
    import JsonProtocol.{authorizationTokenWrites, refreshTokenFormat}
    JwtCookies.withCookie(accessSessionKey, accessToken) +:
      accessToken.refreshToken.toList
        .map(rt => JwtCookies.withCookie(refreshSessionKey, rt))
  }

  // Todo S <: SignerV2
  def extractAuthCode(request: Request[AnyContent]): Option[AuthorizationCode] = {
    import JsonProtocol.authorizationTokenReads
    JwtCookies.extractCookie(accessSessionKey, request)
  }

  def extractRefreshToken(request: Request[AnyContent]): Option[RefreshToken] = {
    import JsonProtocol.refreshTokenFormat
    JwtCookies.extractCookie[RefreshToken](refreshSessionKey, request)
  }

  val discardCookies: List[DiscardingCookie] =
    List(DiscardingCookie(accessSessionKey), DiscardingCookie(refreshSessionKey))
}

case object DiscogsCookies {
  final val sessionKey: String = "discogs4s_session"

  def accessCookie[A](token: A)(implicit writes: Writes[A]): Cookie =
    JwtCookies.withCookie[A](sessionKey, token)

  def extract[A](request: Request[AnyContent])(implicit reads: Reads[A]): Option[A] =
    JwtCookies.extractCookie[A](sessionKey, request)

  val discardCookies: DiscardingCookie = DiscardingCookie(sessionKey)
}

object JwtCookies {

  implicit val clock: Clock = Clock.systemUTC
  implicit val conf: Configuration = Configuration.reference

  private val jwtToken = "token"
  private val path = "/"
  private val domain = None
  private val secure = false

  def extractCookie[A](cookieKey: String, request: Request[AnyContent])(
    implicit reads: Reads[A]
  ): Option[A] =
    request.cookies
      .get(cookieKey)
      .flatMap { cookie =>
        JwtSession.deserialize(cookie.value).getAs[A](jwtToken)
      }

  def withCookie[A](cookieName: String, obj: A)(implicit writes: Writes[A]): Cookie =
    Cookie(
      name = cookieName,
      value = (JwtSession() + (jwtToken, obj)).serialize,
      maxAge = Some(31556952),
      path,
      domain,
      secure,
      httpOnly = true,
      sameSite = None
    )

  def discard(cookieName: String): DiscardingCookie = DiscardingCookie(cookieName, path, domain, secure)
}
