package controllers

import javax.inject.{Inject, Singleton}

import model.music.{JSONParser, MusicCollection}
import model.genetic.{GA, Playlist}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}

/**
  *
  */
@Singleton
class PlaylistController @Inject() extends Controller {

  // @see https://www.playframework.com/documentation/2.0/ScalaJsonRequests
  def generatePlaylist = Action(parse.json) { implicit request =>
    println(request.body.toString())
    JSONParser.parseRequest(request.body) match {
      case None => BadRequest("Json Request failed")
      case Some(p) =>
        val db = new MusicCollection(
          p.ids.flatMap(id => MongoController.readByID(id))
        )
        val (playlist, _) = GA.generatePlaylist(db, p.constraints, p.length)
        val js = createJsonResponse(p.name, playlist)
        Ok(js)
    }
  }

  /**
    * The JSON response to send back to the user
    * @param name the name of the playlist
    * @param playlist the Playlist generated
    * @return a JSON with the name of the playlist and an array of tracks ids
    */
  def createJsonResponse(name: String, playlist: Playlist): JsValue = {
    val tracksID = Json.toJson(playlist.songs.map(s => s.id))
    Json.obj(
      "name" -> name,
      "ids" -> tracksID
    )
  }

}