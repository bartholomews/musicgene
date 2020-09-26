package io.bartholomews.musicgene.controllers.http

import java.time.Clock

import io.bartholomews.fsclient.entities.oauth.AuthorizationCode
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.RefreshToken
import io.bartholomews.musicgene.controllers.http.codecs.FsClientCodecs.{
  authorizationTokenReads,
  authorizationTokenWrites,
  refreshTokenFormat
}
import pdi.jwt.JwtSession
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._

case object SpotifyCookies {
  object SessionKey {
    def access(sessionNumber: Int): String = s"spotify4s_access_session_$sessionNumber"
    def refresh(sessionNumber: Int): String = s"spotify4s_refresh_session_$sessionNumber"
  }

  def extractAllSessionNumbers(implicit request: Request[AnyContent]): List[String] =
    request.cookies
      .map(_.name)
      .collect({
        case s"spotify4s_access_session_$sessionNumber" => sessionNumber
      })
      .toList

  def accessCookies(accessToken: AuthorizationCode)(implicit request: Request[AnyContent]): List[Cookie] = {
    val maybeSessionNumber = request.attrs.get(SessionKeys.spotifySessionKey)
    println(s"accessCookies => $maybeSessionNumber")
    maybeSessionNumber.fold(List.empty[Cookie])(sessionNumber =>
      JwtCookies.withCookie(SessionKey.access(sessionNumber), accessToken) +:
        accessToken.refreshToken.toList
          .map(rt => JwtCookies.withCookie(SessionKey.refresh(sessionNumber), rt))
    )
  }

  // Todo S <: SignerV2
  def extractAuthCode(request: Request[AnyContent]): Option[AuthorizationCode] = {
    val maybeSessionNumber = request.attrs.get(SessionKeys.spotifySessionKey)
    println(s"extractAuthCode => $maybeSessionNumber")
    maybeSessionNumber.flatMap(sessionNumber =>
      JwtCookies.extractCookie[AuthorizationCode](SessionKey.access(sessionNumber), request)
    )
  }

  def extractRefreshToken(request: Request[AnyContent]): Option[RefreshToken] = {
    val maybeSessionNumber = request.attrs.get(SessionKeys.spotifySessionKey)
    println(s"extractRefreshToken => $maybeSessionNumber")
    maybeSessionNumber.flatMap(sessionNumber =>
      JwtCookies.extractCookie[RefreshToken](SessionKey.refresh(sessionNumber), request)
    )
  }

  def discardCookies(implicit request: Request[AnyContent]): List[DiscardingCookie] = {
    val maybeSessionNumber = request.attrs.get(SessionKeys.spotifySessionKey)
    println(s"discardCookies => $maybeSessionNumber")
    maybeSessionNumber
      .fold(List.empty[DiscardingCookie])(sessionNumber =>
        List(
          DiscardingCookie(SessionKey.access(sessionNumber)),
          DiscardingCookie(SessionKey.refresh(sessionNumber))
        )
      )
  }
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

object SessionKeys {
  val spotifySessionKey: TypedKey[Int] = TypedKey[Int](displayName = "spotify_session")
}
