package io.bartholomews.musicgene.controllers.http.codecs

import io.bartholomews.fsclient.core.oauth._
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import sttp.model.Uri

object FsClientCodecs extends CodecsConfiguration {
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

  implicit val tokenFormat: OFormat[Token] = Json.format[Token]
  implicit val tokenWrites: OWrites[Token] = Json.writes[Token]
  implicit val tokenReads: Reads[Token] =
    (JsPath \ "value").read[String].and((JsPath \ "secret").read[String])(Token.apply _)

  implicit val consumerFormat: OFormat[Consumer] = Json.format[Consumer]
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

  implicit val signatureMethodReads: Reads[SignatureMethod] = (json: JsValue) =>
    json.validate[String].flatMap {
      case SignatureMethod.PLAINTEXT.value => JsSuccess(SignatureMethod.PLAINTEXT)
      case SignatureMethod.SHA1.value      => JsSuccess(SignatureMethod.SHA1)
      case other                           => JsError(s"Unknown signature method: [$other]")
    }

  implicit val signatureMethodWrites: Writes[SignatureMethod] = sig => JsString(sig.value)

  implicit val accessTokenV1Format: OFormat[AccessTokenCredentials] = Json.format[AccessTokenCredentials]
//  implicit val accessTokenV1Writes: OWrites[AccessTokenCredentials] = Json.writes[AccessTokenCredentials]
//  implicit val accessTokenV1Reads: Reads[AccessTokenCredentials] =
//    (JsPath \ "token").read[Token].and((JsPath \ "consumer").read[Consumer])(AccessTokenCredentials.apply _)

  implicit val clientPasswordBasicAuthenticationV2Writes: OWrites[ClientPasswordBasicAuthenticationV2] =
    Json.writes[ClientPasswordBasicAuthenticationV2]
  implicit val clientPasswordBasicAuthenticationV2Reads: Reads[ClientPasswordBasicAuthenticationV2] =
    (JsPath \ "client_password").read[ClientPassword].map(ClientPasswordBasicAuthenticationV2.apply)

  implicit val accessTokenSignerV2Format: OFormat[AccessTokenSignerV2] = Json.format[AccessTokenSignerV2]

  import play.api.libs.json._

  implicit val uriReads: Reads[Uri] =
    _.validate[String].flatMap(str => Uri.parse(str).fold(err => JsError(err), uri => JsSuccess(uri)))

  implicit val uriWrites: Writes[Uri] = uri => JsString(uri.toString)

  implicit val resourceOwnerAuthorizationUri: Format[ResourceOwnerAuthorizationUri] =
    Json.valueFormat[ResourceOwnerAuthorizationUri]

  implicit val temporaryCredentialsFormat: OFormat[TemporaryCredentials] = Json.format[TemporaryCredentials]

  //  implicit val uriWrites: Writes[Uri] = Json.writes[String].contramap(_.toString)
  //   implicit val signatureMethodFormat: OFormat[SignatureMethod] = withDiscriminator.format[SignatureMethod]

  //  implicit val requestTokenV1Writes: OWrites[TemporaryCredentials] = Json.writes[TemporaryCredentials]
//  implicit val requestTokenV1Reads: Reads[TemporaryCredentials] =
//    (JsPath \ "token").read[Token].and((JsPath \ "callback_confirmed").read[Boolean])(TemporaryCredentials.apply _)
}
