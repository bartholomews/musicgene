package controllers

import javax.inject._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class AuthController @Inject() extends Controller {
  val spotify = SpotifyController.getInstance

  def auth = Action { Redirect(spotify.getAuthorizeURL) }

  /*
  play.api.mvc.Action type is a wrapper
  around the type `Request[A] => Result`,
  where `Request[A]` identifies an HTTP request
  and `Result` is an HTTP response.
  */
  def callback = Action {
    request => request.getQueryString("code") match {
      case Some(code) =>
        spotify.getAccessToken(code)
        Ok(views.html.callback("Welcome, " + spotify.getName))
      case None => BadRequest("Something went wrong! Please go back and try again.")
    }
    /**
      * TODO
      * is badRequest a proper return code for each?
      * if VALUES: [access_denied] return badRequest(index.render(...));
      * else (if VALUES: [invalid_scope] or something else)
      * return badRequest(callback.render(map.get("error")[0]));
      */
  }

}
