package controllers

import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.{AccessToken, RefreshToken}
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, NonRefreshableToken, Scope, SignerV2}
import play.api.libs.functional.syntax._
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

trait JsonProtocol {

  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)

  implicit val scopeReads: Reads[Scope] = (JsPath \ "scope" \ "values").read[List[String]].map(Scope.apply)
  implicit val scopeWrites: Writes[Scope] = Json.writes[Scope]
  implicit val accessTokenWrites: Writes[AccessToken] = (ac: AccessToken) => JsString(ac.value)
  implicit val accessTokenReads: Reads[AccessToken] = (JsPath \ "access_token").read[String].map(AccessToken.apply)
  implicit val refreshTokenWrites: Writes[RefreshToken] = (rt: RefreshToken) => JsString(rt.value)
  implicit val refreshTokenReads: Reads[RefreshToken] = (JsPath \ "refresh_token").read[String].map(RefreshToken.apply)

  private val tokenTypeReads: Reads[String] = (JsPath \ "token_type").read[String]

  implicit val signerV2Format: OFormat[SignerV2] = Json.format[SignerV2]

  implicit val nonRefreshableTokenWrites: OWrites[NonRefreshableToken] = Json.writes[NonRefreshableToken]
  implicit val nonRefreshableTokenReads: Reads[NonRefreshableToken] = (
    accessTokenReads and
      tokenTypeReads and
      (JsPath \ "expiresIn").read[Long] and
      scopeReads
    ) (NonRefreshableToken.apply _)

  implicit val authorizationTokenWrites: OWrites[AuthorizationCode] = Json.writes[AuthorizationCode]
  implicit val authorizationTokenReads: Reads[AuthorizationCode] = (
    accessTokenReads and
      tokenTypeReads and
      (JsPath \ "expires_in").read[Long] and
      refreshTokenReads and
      scopeReads
    ) (AuthorizationCode.apply _)
}

object JsonProtocol extends JsonProtocol
