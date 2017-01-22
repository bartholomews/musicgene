package controllers.wrapper

/**
  * Spotify API Scala Play wrapper
  */
import javax.inject.Inject

import logging.AccessLogging
import model.entities.{NO_SCOPE, Scope}
import play.api.libs.json.{JsError, _}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{Action, Controller, Result}
import utils.ConversionUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Api @Inject()(configuration: play.api.Configuration, wSClient: WSClient) extends Controller with AccessLogging {
  private val CLIENT_ID = configuration.underlying.getString("CLIENT_ID")
  private val CLIENT_SECRET = configuration.underlying.getString("CLIENT_SECRET")
  private val REDIRECT_URI = configuration.underlying.getString("REDIRECT_URI")
  private val API_BASE_URL = configuration.underlying.getString("API_BASE_URL")
  val AUTHORIZE_ENDPOINT = configuration.underlying.getString("AUTHORIZE_ENDPOINT")
  val TOKEN_ENDPOINT = configuration.underlying.getString("TOKEN_ENDPOINT")

  def test = Action { Redirect(authorizeURL) }

  def featuredPlaylists() = Action.async {
    withLogger(clientCredentials) { response =>
      Ok("")
    }
  }

  def callback = Action.async {
    request => request.getQueryString("code") match {
      case Some(code) => tryWithRequest { accessToken(code) }
      case None => request.getQueryString("error") match {
        case Some("access_denied") => Future(BadRequest("You need to authorize permissions in order to use the App."))
        case Some(error) => Future(BadRequest(error))
        case _ => Future(BadRequest("Something went wrong."))
      }
    }
  }

  def tryWithRequest(request: Future[WSResponse]): Future[Result] = {
    withLogger(request) { response =>
      response.json.validate[Token] match {
        case JsSuccess(t: Token, _) => Ok(views.html.test(t.access_token))
        case JsError(errors) => throw new Exception(errors.head._2.toList.head.message)
      }
    }
  }

  val token: Future[String] = clientCredentials map {
    response: WSResponse => (response.json \ "access_token").as[String]
  }

  def withClientCredentials(action: Token => Result): Future[Result] = {
    withLogger(clientCredentials) { response =>
      response.json.validate[Token] match {
        case t: Token => action(t)
        case _ => throw new Exception("WTF")
      }
    }
  }

  def test2 = Action.async {
    withClientCredentials(token => Ok(token.access_token))
  }

  def test3 = Action.async {
    withLogger(clientCredentials) { response =>
      Ok(views.html.test { (response.json \ "access_token").as[String] })
    }
  }

  def OkTest(s: String) = Ok(views.html.test(s))

  def tokenFlow(token: Future[Token]): Future[Result] = token map {
    response => {
      Ok(views.html.test(response.access_token))
    }
  }

  def play = Action.async {
    withLogger(clientCredentials) { _ => Ok("") }
  }

  val BROWSE_FEATURED_PLAYLISTS = "/browse/featured-playlists"
  def featuredPlaylist(implicit token: String): Future[WSResponse] = {
    wSClient.url(API_BASE_URL + BROWSE_FEATURED_PLAYLISTS)
      .withHeaders(auth_bearer(token))
      .withQueryString(
        "" -> ""
      )
      .get()
  }
  // HTTP/1.1 400 Bad Request

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

    wSClient.url(AUTHORIZE_ENDPOINT)
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

  def clientCredentials: Future[WSResponse] = {
    wSClient.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map("grant_type" -> Seq("client_credentials")))
  }

  def accessToken(code: String): Future[WSResponse] = {
    wSClient.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("authorization_code"),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(REDIRECT_URI)
      ))
  }

  def refreshToken(refreshToken: String): Future[WSResponse] = {
    wSClient.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("refresh_token"),
        "refresh_token" -> Seq(refreshToken)
      ))
  }


  /*
  // WSResponse wrapped in a Future, @see https://www.playframework.com/documentation/2.5.x/ScalaWS
  def withToken(token: Option[Token] = None)(implicit f: Future[Token] => SpotifyObject): SpotifyObject = {
    f(token match {
      case None => tryWithTokenRequest(clientCredentials)
      case Some(t) =>
        if (!t.expired) Future(t)
        else t.refresh_token match {
          // token is a refresh token
          case None => tryWithTokenRequest(refreshToken(t.access_token))
          // token is an auth_token
          case Some(refresh_token) => tryWithTokenRequest(refreshToken(refresh_token))
        }
    })
  }
  */

  private val auth_headers = {
    val base64_secret = ConversionUtils.base64(s"$CLIENT_ID:$CLIENT_SECRET")
    "Accept" -> "application/json"
    "Authorization" -> s"Basic $base64_secret"
  }

  private def auth_bearer(token: String) = { "Authorization" -> s"Bearer $token" }

  // SHOULD GO IN SEPARATE CONTROLLER, THIS CLASS SHOULD BE A WRAPPER NOT A CONTROLLER
  private def authorizeURL: String = authorizeURL(CLIENT_ID, REDIRECT_URI, None, List()).uri.toString

}
