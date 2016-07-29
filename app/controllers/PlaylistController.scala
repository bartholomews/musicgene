package controllers

import javax.inject.{Inject, Singleton}

import model.constraints.Constraint
import model.music.{Cache, MusicCollection, MusicUtil, Song}
import model.geneticScala.{GA, Playlist, StandardFitness}
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
  doesn’t meet that expectation. Hence we don’t need to check again
  in our action code.
   */
  def generatePlaylist =  Action(parse.json) { implicit request =>

    println("received POST request for " + request.body.toString())

    val (name, constraints) = MusicUtil.parseRequest(request.body)
    println("CONSTR =====> " + constraints.toString())

    val ids = MusicUtil.parseIDS(request.body)
    println("IDS ========> " + ids.foreach(s => println(s)))

    // if a constraint with track number is selected, otherwise 20
    // ALL THIS STUFF SHOULD BE MOVED TO ITS OWN CLASS
    val n = 20

    def getPlaylist: Playlist = {
      if (ids.isEmpty) GA.generatePlaylist(constraints, 20)
      else {
        val songs = Cache.extractSongs(ids)
        songs.foreach(s => println(s.title))
        GA.generatePlaylist(new MusicCollection(songs), constraints, 20)
      }
    }
    val playlist = getPlaylist.songs

    val tracksID = Json.toJson(playlist.map(s => s.id))
    val js: JsValue = Json.obj(
      "name" -> name,
      "ids" -> tracksID
    )

    println("READY TO SEND:")
    println(playlist.foreach(s => println(s.artist + " - " + s.title)))

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

  /*
  def generatePlaylist(ids: List[String], constraints: List[(String, List[String])]) = Action {
    val cons: Set[Constraint] = constraints.map(c => MusicUtil.parseRequest(c)).toSet
    ids.foreach(song => println(song))
    cons.foreach(c => println(c))
    Ok(views.html.index("GEN"))
  }
  */

}
