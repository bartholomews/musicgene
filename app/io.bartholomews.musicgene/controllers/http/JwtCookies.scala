package io.bartholomews.musicgene.controllers.http

import java.time.Clock

import io.bartholomews.fsclient.core.oauth.AuthorizationCode
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RefreshToken
import io.bartholomews.musicgene.controllers.http.codecs.FsClientCodecs.{
  authorizationTokenReads,
  authorizationTokenWrites,
  refreshTokenFormat
}
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import pdi.jwt.JwtSession
import play.api.libs.json.{Reads, Writes}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import play.api.{Configuration, Logging}

case object SpotifyCookies extends Logging {
  object SessionKey {
    def access(session: SpotifySessionUser): String = s"spotify4s_access_session_${session.entryName}"
    def refresh(session: SpotifySessionUser): String = s"spotify4s_refresh_session_${session.entryName}"
  }

  def accessCookies(accessToken: AuthorizationCode)(implicit request: Request[AnyContent]): List[Cookie] = {
    val maybeSession = request.attrs.get(SpotifySessionKeys.spotifySessionUser)
    logger.debug(s"accessCookies => $maybeSession")
    maybeSession.fold(List.empty[Cookie])(session =>
      JwtCookies.withCookie(SessionKey.access(session), accessToken) +:
        accessToken.refreshToken.toList
          .map(rt => JwtCookies.withCookie(SessionKey.refresh(session), rt))
    )
  }

  def extractAuthCode(session: SpotifySessionUser)(implicit request: Request[AnyContent]): Option[AuthorizationCode] = {
    logger.debug(s"extractAuthCode => $session")
    JwtCookies.extractCookie[AuthorizationCode](SessionKey.access(session), request)
  }

  // Todo S <: SignerV2
  def extractAuthCode(request: Request[AnyContent]): Option[AuthorizationCode] = {
    val maybeSession = request.attrs.get(SpotifySessionKeys.spotifySessionUser)
    logger.debug(s"extractAuthCode => $maybeSession")
    maybeSession.flatMap(session => JwtCookies.extractCookie[AuthorizationCode](SessionKey.access(session), request))
  }

  def extractRefreshToken(request: Request[AnyContent]): Option[RefreshToken] = {
    val maybeSession = request.attrs.get(SpotifySessionKeys.spotifySessionUser)
    logger.debug(s"extractRefreshToken => $maybeSession")
    maybeSession.flatMap(session => JwtCookies.extractCookie[RefreshToken](SessionKey.refresh(session), request))
  }

  def discardCookies(session: SpotifySessionUser): List[DiscardingCookie] = {
    logger.debug(s"discardCookies => $session")
    List(
      DiscardingCookie(SessionKey.access(session)),
      DiscardingCookie(SessionKey.refresh(session))
    )
  }

  def discardAllCookies: List[DiscardingCookie] =
    SpotifySessionUser.values.toList.flatMap(discardCookies)
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

object SpotifySessionKeys {
  val spotifySessionUser: TypedKey[SpotifySessionUser] = TypedKey[SpotifySessionUser](displayName = "spotify_session")
}
