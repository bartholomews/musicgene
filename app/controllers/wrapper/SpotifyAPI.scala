package controllers.wrapper

import play.api.Logger
import javax.inject.Inject

import logging.AccessLogging
import model.entities._
import play.api.libs.json.{JsError, _}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{Action, Controller, Result}
import utils.ConversionUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAPI @Inject()(configuration: play.api.Configuration, ws: WSClient) extends Controller with AccessLogging {
  private val CLIENT_ID = configuration.underlying.getString("CLIENT_ID")
  private val CLIENT_SECRET = configuration.underlying.getString("CLIENT_SECRET")
  private val REDIRECT_URI = configuration.underlying.getString("REDIRECT_URI")
  val API_BASE_URL = configuration.underlying.getString("API_BASE_URL")
  val AUTHORIZE_ENDPOINT = configuration.underlying.getString("AUTHORIZE_ENDPOINT")
  val TOKEN_ENDPOINT = configuration.underlying.getString("TOKEN_ENDPOINT")

  private var access_token: Option[Future[Token]] = None

  def refresh: Future[Token] = getToken(logRequest(clientCredentials))

  def validate[T](f: Future[WSResponse])(implicit fmt: Reads[T]): Future[T] = {
    f map { response =>
      response.json.validate[T](fmt) match {
        case JsSuccess(a, _) => a
        case JsError(errors) => throw new Exception(errors.toString) //.head._2.toList.head.messages.toString())
      }
    }
  }

  def withToken(request: Token => Future[Result]): Future[Result] = {
    access_token match {
      case None => access_token = Some(refresh); withToken(request)
      case Some(t) => t flatMap  { token =>
          access_token = if (token.expired) Some(refresh) else access_token
          request(token)
      }
    }
  }

  // TODO Redirect(authorizeURL)

  def callback = Action.async {
    request => request.getQueryString("code") match {
      case Some(code) => withLogger(accessToken(code)) { response: WSResponse => Ok("OKOKOKOK") }
      case None => request.getQueryString("error") match {
        case Some("access_denied") => Future(BadRequest("You need to authorize permissions in order to use the App."))
        case Some(error) => Future(BadRequest(error))
        case _ => Future(BadRequest("Something went wrong."))
      }
    }
  }

  def getToken(request: Future[WSResponse]): Future[Token] = {
    request map { response =>
      response.json.validate[Token] match {
        case JsSuccess(t: Token, _) => t
        case JsError(errors) => throw new Exception(errors.head._2.toList.head.message)
      }
    }
  }

  def clientCredentials: Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map("grant_type" -> Seq("client_credentials")))
  }

  def accessToken(code: String): Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("authorization_code"),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(REDIRECT_URI)
      ))
  }

  def refreshToken(refreshToken: String): Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("refresh_token"),
        "refresh_token" -> Seq(refreshToken)
      ))
  }

  val auth_headers = {
    val base64_secret = ConversionUtils.base64(s"$CLIENT_ID:$CLIENT_SECRET")
    //"Accept" -> "application/json"
    "Authorization" -> s"Basic $base64_secret"
  }

  def auth_bearer(token: String) = {
    "Authorization" -> s"Bearer $token"
  }

  // SHOULD GO IN SEPARATE CONTROLLER, THIS CLASS SHOULD BE A WRAPPER NOT A CONTROLLER
  def authorizeURL: String = authorizeURL(CLIENT_ID, REDIRECT_URI, None, List()).uri.toString

  /**
    * @see https://developer.spotify.com/web-api/authorization-guide/
    * @param client_id    Required. The client ID provided to you by Spotify when you register your application.
    * @param redirect_uri Required. The URI to redirect to after the user grants/denies permission.
    *                     This URI needs to have been entered in the Redirect URI whitelist
    *                     that you specified when you registered your application.
    *                     The value of redirect_uri here must exactly match one of the values
    *                     you entered when you registered your application,
    *                     including upper/lowercase, terminating slashes, etc.
    * @param state        Optional, but strongly recommended. The state can be useful for correlating requests and responses.
    *                     Because your redirect_uri can be guessed, using a state value can increase your assurance
    *                     that an incoming connection is the result of an authentication request.
    *                     If you generate a random string or encode the hash of some client state (e.g., a cookie)
    *                     in this state variable, you can validate the response to additionally ensure
    *                     that the request and response originated in the same browser.
    *                     This provides protection against attacks such as cross-site request forgery.
    * @see RFC-6749 [https://tools.ietf.org/html/rfc6749#section-10.12]
    * @param scopes      Optional. A space-separated list of scopes: @see `Scope`
    *                    If no scopes are specified, authorization will be granted only
    *                    to access publicly available information: that is, only information
    *                    normally visible in the Spotify desktop, web and mobile players.
    * @param show_dialog Optional. Whether or not to force the user to approve the app again if they’ve already done so.
    *                    If false (default), a user who has already approved the application
    *                    may be automatically redirected to the URI specified by redirect_uri.
    *                    If true, the user will not be automatically redirected and will have to approve the app again.
    * @return the Spotify URL where the user can grant/deny permissions.
    */
  def authorizeURL(client_id: String, redirect_uri: String, state: Option[String],
                   scopes: List[Scope] = List(NO_SCOPE), show_dialog: Boolean = true): WSRequest = {

    ws.url(AUTHORIZE_ENDPOINT)
      .withHeaders(auth_headers)
      .withQueryString(
        "client_id" -> client_id,
        "response_type" -> "code",
        "redirect_uri" -> redirect_uri
    //    "scope" -> scopes.map(s => s.value).mkString(" "),
    //    "show_dialog" -> show_dialog.toString,
    //    "state" -> state.getOrElse("")
      )
  }


  // HTTP/1.1 400 Bad Request
}
