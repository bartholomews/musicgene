package controllers.http

import io.bartholomews.discogs4s.entities.RequestToken
import io.bartholomews.fsclient.entities.oauth._
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework._
import io.bartholomews.spotify4s.entities.SpotifyId
import org.http4s.client.oauth1.{Consumer, Token}
import play.api.libs.functional.syntax._
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

trait JsonProtocol {

  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)

  implicit val accessTokenFormat: Format[AccessToken] = Json.valueFormat[AccessToken]
  implicit val refreshTokenFormat: Format[RefreshToken] = Json.valueFormat[RefreshToken]
  implicit val clientIdFormat: Format[ClientId] = Json.valueFormat[ClientId]
  implicit val clientSecretFormat: Format[ClientSecret] = Json.valueFormat[ClientSecret]
  implicit val spotifyIdFormat: Format[SpotifyId] = Json.valueFormat[SpotifyId]

  implicit val scopeReads: Reads[Scope] = (JsPath \ "scope" \ "values").read[List[String]].map(Scope.apply)
  implicit val scopeWrites: Writes[Scope] = Json.writes[Scope]

  private val tokenTypeReads: Reads[String] = (JsPath \ "token_type").read[String]

  implicit val clientPasswordWrites: OWrites[ClientPassword] = Json.writes[ClientPassword]
  implicit val clientPasswordReads: Reads[ClientPassword] =
    (JsPath \ "client_id").read[ClientId].and((JsPath \ "client_secret").read[ClientSecret])(ClientPassword.apply _)

  implicit val tokenWrites: OWrites[Token] = Json.writes[Token]
  implicit val tokenReads: Reads[Token] =
    (JsPath \ "value").read[String].and((JsPath \ "secret").read[String])(Token.apply _)

  implicit val consumerWrites: OWrites[Consumer] = Json.writes[Consumer]
  implicit val consumerReads: Reads[Consumer] =
    (JsPath \ "key").read[String].and((JsPath \ "secret").read[String])(Consumer.apply _)

  implicit val nonRefreshableTokenWrites: OWrites[NonRefreshableToken] = Json.writes[NonRefreshableToken]
  implicit val nonRefreshableTokenReads: Reads[NonRefreshableToken] = (
    (JsPath \ "generated_at").read[Long] and
      (JsPath \ "access_token").read[AccessToken] and
      tokenTypeReads and
      (JsPath \ "expiresIn").read[Long] and
      scopeReads
    ) (NonRefreshableToken.apply _)

  implicit val authorizationTokenWrites: OWrites[AuthorizationCode] = Json.writes[AuthorizationCode]
  implicit val authorizationTokenReads: Reads[AuthorizationCode] = (
    (JsPath \ "generated_at").read[Long] and
      (JsPath \ "access_token").read[AccessToken] and
      tokenTypeReads and
      (JsPath \ "expires_in").read[Long] and
      (JsPath \ "refresh_token").readNullable[String].map(_.map(RefreshToken.apply)) and
      scopeReads) (AuthorizationCode.apply _)

  implicit val accessTokenV1Writes: OWrites[AccessTokenCredentials] = Json.writes[AccessTokenCredentials]
  implicit val accessTokenV1Reads: Reads[AccessTokenCredentials] =
    (JsPath \ "token").read[Token].and((JsPath \ "consumer").read[Consumer])(AccessTokenCredentials.apply _)

  implicit val clientPasswordBasicAuthenticationV2Writes: OWrites[ClientPasswordBasicAuthenticationV2] =
    Json.writes[ClientPasswordBasicAuthenticationV2]
  implicit val clientPasswordBasicAuthenticationV2Reads: Reads[ClientPasswordBasicAuthenticationV2] =
    (JsPath \ "client_password").read[ClientPassword].map(ClientPasswordBasicAuthenticationV2.apply)

  implicit val requestTokenV1Writes: OWrites[RequestToken] = Json.writes[RequestToken]
  implicit val requestTokenV1Reads: Reads[RequestToken] =
    (JsPath \ "token").read[Token].and((JsPath \ "callback_confirmed").read[Boolean])(RequestToken.apply _)

  implicit val signerV2Format: OFormat[SignerV2] = Json.format[SignerV2]
  implicit val accessTokenSignerV2Format: OFormat[AccessTokenSignerV2] = Json.format[AccessTokenSignerV2]
}

object JsonProtocol extends JsonProtocol
