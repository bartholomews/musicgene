package controllers.wrapper

/**
  * Spotify API Scala Play wrapper
  */
import javax.inject.{Inject, Singleton}

import logging.AccessLogging
import controllers.wrapper.entities._
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, _}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import utils.ConversionUtils

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class BaseApi @Inject()(configuration: play.api.Configuration, ws: WSClient) extends AccessLogging {

  val BASE_URL: String = configuration.underlying.getString("API_BASE_URL")
  val AUTHORIZE_ENDPOINT = configuration.underlying.getString("AUTHORIZE_ENDPOINT")
  val TOKEN_ENDPOINT = configuration.underlying.getString("TOKEN_ENDPOINT")

  /* REMOVE THIS
def getAll[T](page: Page[T])(call: String => Page[T]): List[T] = {
  @tailrec
  def loop(p: Page[T], acc: List[T]): List[T] = {
    p.next match {
      case None => acc
      case Some(href) =>
        val pg = call(href)
        loop(pg, acc ::: pg.items)
    }
  }
  loop(page, page.items)
}
*/

  def getAll[T](call: String => Future[Page[T]])(endpoint: String): Future[List[T]] = {
    def loop(p: Page[T], acc: List[T]): Future[List[T]] = {
      p.next match {
        case null => Future(acc)
        case None => Future(acc)
        case Some(href) =>
          val future: Future[Page[T]] = call(href)
          future flatMap {
            p => loop(p, acc ::: p.items)
          }
      }
    }
    call(endpoint) flatMap {
      p => loop(p, p.items)
    }
  }

  def getAll[T](page: Page[T])(call: String => Future[Page[T]]): Future[List[T]] = {
    def loop(p: Page[T], acc: List[T]): Future[List[T]] = {
      p.next match {
        case None => Future(acc)
        case Some(href) =>
          val future: Future[Page[T]] = call(href)
          future flatMap {
            p => loop(p, acc ::: p.items)
          }
      }
    }
    loop(page, page.items)
  }

  def getWithLogger[T](endpoint: String)(implicit fmt: Reads[T]): Future[T] = {
    withToken[T](t => validate[T] {
      logRequest {
        ws.url(endpoint)
          .withHeaders(auth_bearer(t.access_token))
          .get()
      }
    }(fmt))
  }

  def get[T](endpoint: String)(implicit fmt: Reads[T]): Future[T] = {
    withToken[T](t => validate[T] {
        ws.url(endpoint)
          .withHeaders(auth_bearer(t.access_token))
          .get()
    }(fmt))
  }

  def get[T](key: String)(endpoint: String)(implicit fmt: Reads[T]): Future[T] = {
    withToken[T](t => validate[T] {
      ws.url(endpoint)
        .withHeaders(auth_bearer(t.access_token))
        .get()
    }(fmt))
  }

  /*
  def getList[T](endpoints: List[String])(implicit fmt: Reads[T]): Future[List[T]] = {
    val list: List[Future[T]] = endpoints map (e => get[T](e))
    val listTry: List[Future[Try[T]]] = list.map(futureToFutureTry)
    // TODO log failures
    Future.sequence(listTry).map(_.collect { case Success(x) => x })
  }
  */

  /**
    * Collect disregarding failures
    * @see http://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures
    * @param list
    * @tparam T
    * @return
    */
  def getFutureList[T](list: List[Future[T]]): Future[List[T]] = {
    Future.sequence(
      list.map(futureToFutureTry)
    ).map(_.collect { case Success(x) => x })
  }

  // @see http://stackoverflow.com/a/20874404
  private def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = {
    f.map(Success(_)).recover({case e => Failure(e) })
  }

  /*
  def getWithOAuth[T](endpoint: String, logger: Boolean = false)(implicit fmt: Reads[T]): Future[T] = {
    if (logger) {
      withAuthToken()(t => validate[T] {
        logRequest {
          ws.url(endpoint)
            .withHeaders(auth_bearer(t.access_token))
            .get()
        }
      }(fmt))
    } else getWithOAuth(endpoint)
  }
  */

  def getWithOAuth[T](endpoint: String)(implicit fmt: Reads[T]): Future[T] = {
    withAuthToken()(t => validate[T] {
        ws.url(endpoint)
          .withHeaders(auth_bearer(t.access_token))
          .get()
    }(fmt))
  }

  def validate[T](f: Future[WSResponse])(implicit fmt: Reads[T]): Future[T] = {
    f map { response =>
      response.json.validate[T](fmt) match {
        case JsSuccess(obj, _) => obj
        case JsError(errors) => // throw new Exception(handleError(response, errors))
          accessLogger.debug(errors.toString)
          val error = response.json \ "error"//.validate[String].get
          // TODO CATCH PROPERLY (ALSO THERE ARE 2 DIFFERENT TYPES OF JSON ERROR RESPONSES, SEE ENTITIES)
          // val error_description = response.json \ "error_description"//.validate[String].get
          val error_message = (error \ "message").validate[String].get
          throw new Exception(error_message)
      }
    }
  }

  // TODO Future.failed[T] instead of Exception, ALSO should be able to detect from the first JsError above
  // path which kind of error is that instead of try-matching blindly
  def handleError[T](response: WSResponse, errors: Seq[(JsPath, Seq[ValidationError])]): String = {
    accessLogger.debug(response.json.toString)
    response.json.validate[AuthError] match {
      case JsSuccess(obj, _) => obj.error
      case JsError(_) => response.json.validate[RegularError] match {
        case JsSuccess(obj, _) => obj.message
        case JsError(_) => errors.head.toString
      }
    }
  }

  def validate[T](key: String)(f: Future[WSResponse])(implicit fmt: Reads[T]): Future[T] = {
    f map { response =>
      (response.json \ key).validate[T](fmt) match {
        case JsSuccess(obj, _) => obj
        case JsError(errors) =>
          //val error = (response.json \ "error").validate[String].get
          //val error_description = (response.json \ "error_description").validate[String].get
          throw new Exception(errors.head.toString) // (s"$error: $error_description")
      }
    }
  }

  def tryValidate[T](f: Future[WSResponse])(implicit fmt: Reads[T]): Future[Try[T]] = {
    f map { response =>
      response.json.validate[T](fmt) match {
        case JsSuccess(obj, _) => Try(obj)
        case JsError(errors) =>
          val error = (response.json \ "error").validate[String].get
          val error_description = (response.json \ "error_description").validate[String].get
          Failure {
            throw new Exception(s"$error: $error_description")
          }
      }
    }
  }

  // * =========================================== AUTH // ====================================================== * //

  private val CLIENT_ID = configuration.underlying.getString("CLIENT_ID")
  private val CLIENT_SECRET = configuration.underlying.getString("CLIENT_SECRET")
  private val REDIRECT_URI = configuration.underlying.getString("REDIRECT_URI")

  @volatile private var authorization_code: Option[Future[Token]] = None
  @volatile private var client_credentials: Option[Future[Token]] = None

  @tailrec
  final def withToken[T](request: Token => Future[T]): Future[T] = {
    client_credentials match {
      case None => client_credentials = Some(refresh); withToken(request)
      case Some(t) => t flatMap { token =>
        client_credentials = if (token.expired) Some(refresh) else client_credentials
        request(token)
      }
    }
  }

  private def refresh: Future[Token] = validate[Token] { logRequest { clientCredentials } }

  def callback[T](authCode: String)(request: Token => Future[T]): Future[T] = {
    authorization_code = Some(access(authCode))
    withAuthToken(Some(authCode))(request)
  }

  @tailrec
  final def withAuthToken[T](authCode: Option[String] = None)(request: Token => Future[T]): Future[T] = {
    authorization_code match {
      case Some(t) => t flatMap {
        token => authorization_code = {
          if (token.expired) Some(refresh(token)) //refresh(token.refresh_token)) // .getOrElse(throw new Exception("No refresh token found"))))
          else authorization_code
        }
          request(token)
      }
      case None =>
        authorization_code = Some(access(authCode.getOrElse(throw new Exception("Authorization code not provided"))))
        withAuthToken(authCode)(request)
    }
  }

  private def refresh(oldToken: Token): Future[Token] = refresh(oldToken.refresh_token.get) map {
    newToken => Token(oldToken.access_token, newToken.token_type, newToken.scope, newToken.expires_in, oldToken.refresh_token)
  }

  private def access(code: String): Future[Token] = validate[Token] { logRequest { accessToken(code) } }
  private def refresh(code: String): Future[Token] = validate[Token] { logRequest { refreshToken(code) } }

  private def accessToken(code: String): Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("authorization_code"),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(REDIRECT_URI)
      ))
  }

  private def refreshToken(refreshToken: String): Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map(
        "grant_type" -> Seq("refresh_token"),
        "refresh_token" -> Seq(refreshToken)
      ))
  }

  private val auth_headers = {
    val base64_secret = ConversionUtils.base64(s"$CLIENT_ID:$CLIENT_SECRET")
    "Accept" -> "application/json"
    "Authorization" -> s"Basic $base64_secret"
  }

  def auth_bearer(token: String): (String, String) = {
    "Authorization" -> s"Bearer $token"
  }

  def authoriseURL: String = authoriseURL()

  def authoriseURL(state: Option[String] = None, scopes: List[Scope] = List(), showDialog: Boolean = true): String = {
    requestAuthoriseURL(CLIENT_ID, REDIRECT_URI, state, scopes, showDialog).uri.toString
  }

  /**
    * @see https://developer.spotify.com/web-api/authorization-guide/
    *
    * @param client_id    Required. The client ID provided to you by Spotify when you register your application.
    *
    * @param redirect_uri Required. The URI to redirect to after the user grants/denies permission.
    *                     This URI needs to have been entered in the Redirect URI whitelist
    *                     that you specified when you registered your application.
    *                     The value of redirect_uri here must exactly match one of the values
    *                     you entered when you registered your application,
    *                     including upper/lowercase, terminating slashes, etc.
    *
    * @param state        Optional, but strongly recommended. The state can be useful for correlating requests and responses.
    *                     Because your redirect_uri can be guessed, using a state value can increase your assurance
    *                     that an incoming connection is the result of an authentication request.
    *                     If you generate a random string or encode the hash of some client state (e.g., a cookie)
    *                     in this state variable, you can validate the response to additionally ensure
    *                     that the request and response originated in the same browser.
    *                     This provides protection against attacks such as cross-site request forgery.
    *
    * @see               RFC-6749 [https://tools.ietf.org/html/rfc6749#section-10.12]
    *
    * @param scopes      Optional. A space-separated list of scopes: @see `Scope`
    *                    If no scopes are specified, authorization will be granted only
    *                    to access publicly available information: that is, only information
    *                    normally visible in the Spotify desktop, web and mobile players.
    *
    * @param show_dialog Optional. Whether or not to force the user to approve the app again if they’ve already done so.
    *                    If false (default), a user who has already approved the application
    *                    may be automatically redirected to the URI specified by redirect_uri.
    *                    If true, the user will not be automatically redirected and will have to approve the app again.
    *
    * @return the Spotify URL where the user can grant/deny permissions.
    */
  private def requestAuthoriseURL(client_id: String, redirect_uri: String, state: Option[String],
                   scopes: List[Scope], show_dialog: Boolean): WSRequest = {

    ws.url(AUTHORIZE_ENDPOINT)
      .withHeaders(auth_headers)
      .withQueryString(
        "client_id" -> client_id,
        "response_type" -> "code",
        "redirect_uri" -> REDIRECT_URI,
        "scope" -> scopes.map(s => s.value).mkString(" "),
        "show_dialog" -> show_dialog.toString,
        "state" -> state.getOrElse("")
      )
  }

  private def clientCredentials: Future[WSResponse] = {
    ws.url(TOKEN_ENDPOINT)
      .withHeaders(auth_headers)
      .post(Map("grant_type" -> Seq("client_credentials")))
  }

}

