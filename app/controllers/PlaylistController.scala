package controllers

import javax.inject.{Inject, Singleton}

import model.constraints._
import model.music.{ConstraintsParser, MusicCollection}
import model.geneticScala.{CostBasedFitness, GA, GAResponse, Playlist}
import play.api.libs.json.{JsValue, Json}
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

    val (name, constraints) = ConstraintsParser.parseRequest(request.body)
    println("PARSED CONSTRAINTS: =====> " + constraints.toString())

    val ids = ConstraintsParser.parseIDS(request.body)
 //   println("PARSED IDS ========> " + ids.toString())

    // if a constraint with track number is selected, otherwise 20
    // ALL THIS STUFF SHOULD BE MOVED TO ITS OWN CLASS
    val n = ConstraintsParser.parseNumberOfTracks(request.body)

    val db = new MusicCollection(ids.flatMap(id => MongoController.readByID(id)))

    // NOT FUNCTIONAL TAKES EXTERNAL VALUES
    def getPlaylist(ids: Vector[String], c: Set[Constraint]): (Playlist, Option[GAResponse]) = {
      if (ids.isEmpty) GA.generatePlaylist(new MusicCollection(MongoController.readAll), CostBasedFitness(c), n)
      else GA.generatePlaylist(db, CostBasedFitness(c), n)
    }

   // val valueConstraints = constraints.filter(c => c.isInstanceOf[MonotonicValue])
   // val retrievedIDs = getPlaylist(ids, valueConstraints)._1.songs.map(s => s.id)

    /*
    c.foreach {
      case x: MonotonicValue => println("FIRST_STEP_BUCKET")
      case x: MonotonicRange => println("TSA_BUCKET")
      case _ => println("UNKNOWN_CONSTRAINT")
    }
    */

    val (playlist, _) = getPlaylist(ids, constraints.asInstanceOf[Set[Constraint]])

    /*
    val stats = statistics match {
      case None => ""
      case Some(s) => s
    }
    */

    val tracksID = Json.toJson(playlist.songs.map(s => s.id))
    val js: JsValue = Json.obj(
      "name" -> name,
      "ids" -> tracksID
    //  "response" -> statistics.get.distance // TODO proper
    )
    println("AJAX RESPONSE => " + js.toString())
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
