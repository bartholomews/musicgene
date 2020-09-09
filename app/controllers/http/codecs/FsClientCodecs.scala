package controllers.http.codecs

import io.bartholomews.discogs4s.entities.RequestToken
import io.bartholomews.fsclient.entities.oauth.v2.OAuthV2AuthorizationFramework.{
  AccessToken,
  ClientId,
  ClientPassword,
  ClientSecret,
  RefreshToken
}
import io.bartholomews.fsclient.entities.oauth.{
  AccessTokenCredentials,
  AccessTokenSignerV2,
  AuthorizationCode,
  ClientPasswordBasicAuthenticationV2,
  NonRefreshableToken,
  Scope,
  SignerV2
}
import org.http4s.client.oauth1.{Consumer, Token}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, JsPath, Json, OFormat, OWrites, Reads, Writes}

object FsClientCodecs {
  implicit val accessTokenFormat: Format[AccessToken] = Json.valueFormat[AccessToken]
  implicit val refreshTokenFormat: Format[RefreshToken] = Json.valueFormat[RefreshToken]
  implicit val clientIdFormat: Format[ClientId] = Json.valueFormat[ClientId]
  implicit val clientSecretFormat: Format[ClientSecret] = Json.valueFormat[ClientSecret]

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

  implicit val nonRefreshableTokenReads: Reads[NonRefreshableToken] =
    (JsPath \ "generated_at")
      .read[Long]
      .and((JsPath \ "access_token").read[AccessToken])
      .and(tokenTypeReads)
      .and((JsPath \ "expiresIn").read[Long])
      .and(scopeReads)(NonRefreshableToken.apply _)

  implicit val authorizationTokenWrites: OWrites[AuthorizationCode] = Json.writes[AuthorizationCode]
  implicit val authorizationTokenReads: Reads[AuthorizationCode] =
    (JsPath \ "generated_at")
      .read[Long]
      .and((JsPath \ "access_token").read[AccessToken])
      .and(tokenTypeReads)
      .and((JsPath \ "expires_in").read[Long])
      .and((JsPath \ "refresh_token").readNullable[RefreshToken])
      .and(scopeReads)(AuthorizationCode.apply _)

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
