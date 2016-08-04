package controllers

import javax.inject.{Inject, Singleton}

import model.constraints.{Constraint, ScoreConstraint, UnaryConstraint}
import model.music.{Cache, MusicCollection, MusicUtil, Song}
import model.geneticScala.{CostBasedFitness, GA, Playlist, StandardFitness}
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc.{Action, Controller}

import scala.util.parsing.json.JSONArray

/**
  *
  */
@Singleton
class PlaylistController @Inject() extends Controller {

  // @see https://www.playframework.com/documentation/2.0/ScalaJsonRequests
  /*
  The reason why it’s not an Option is because the json body parser
  will validate that the request has a Content-Type of application/json,
  and send back a 415 Unsupported Media Type response if the request
  doesn't meet that expectation. Hence we don’t need to check again
  in our action code.
   */
  def generatePlaylist =  Action(parse.json) { implicit request =>

    println("received POST request for " + request.body.toString())

    val (name, constraints) = MusicUtil.parseRequest(request.body)
    println("PARSED CONSTRAINTS: =====> " + constraints.toString())

    val ids = MusicUtil.parseIDS(request.body)
 //   println("PARSED IDS ========> " + ids.toString())

    // if a constraint with track number is selected, otherwise 20
    // ALL THIS STUFF SHOULD BE MOVED TO ITS OWN CLASS
    val n = MusicUtil.parseNumberOfTracks(request.body)

    // how to determine specific kind of constraint?
    val unary = constraints.asInstanceOf[Set[ScoreConstraint]]

    def getPlaylist: Playlist = {
      if (ids.isEmpty) GA.generatePlaylist(CostBasedFitness(unary), n)
      else {
        val songs = Cache.getFromCache(ids)._1
        GA.generatePlaylist(new MusicCollection(songs), CostBasedFitness(unary), n)
      }
    }
    val playlist = getPlaylist.songs

    val tracksID = Json.toJson(playlist.map(s => s.id))
    val js: JsValue = Json.obj(
      "name" -> name,
      "ids" -> tracksID
    )
    Ok(js)
  }

    /*
    (request.body \ "constraints").asOpt[String].map { name =>
      println("SUCCESSFULLY RETRIEVED JSON " + name)
      Ok(views.html.index("GEN"))
    }.getOrElse {
      BadRequest("Missing parameter")
    }
    */
 //   playlist.prettyPrint()

}
