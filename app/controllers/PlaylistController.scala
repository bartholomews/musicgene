//package controllers
//
//import javax.inject.{Inject, Singleton}
//
//import model.music.{JSONParser, MusicCollection, PlaylistRequest, Song}
//import model.genetic.{GA, Playlist}
//import play.api.cache.redis.CacheApi
//import play.api.libs.json.{JsValue, Json}
//import play.api.mvc.{Action, Controller}
//
///**
//  * Controller to handle playlists requests/responses
//  *
//  * @param configuration the MongoDB server configuration injected from .conf file when the application starts
//  */
//@Singleton
//class PlaylistController @Inject() (configuration: play.api.Configuration, cache: CacheApi) extends Controller {
//
//  /**
//    * The 'tracks' collection at injected MongoDB server
//    */
//  val dbTracks = MongoController.getCollection(
//    configuration.underlying.getString("mongodb.uri"),
//    configuration.underlying.getString("mongodb.db"),
//    configuration.underlying.getString("mongodb.tracks")
//  )
//
//  // @see https://www.playframework.com/documentation/2.0/ScalaJsonRequests
//  /**
//    * Handle a JSON HTTP request with a Content-type header as text/json or application/json
//    * The body of the request is parsed by the `JSONParser` object
//    * which will return an Option[PlaylistRequest].
//    * If the request is parsed correctly, the playlist generation algorithm
//    * will be started with the data from the PlaylistRequest
//    * and a 200 Ok with the JSON response will return,
//    * else a 400 Bad Request
//    *
//    * @return a 200 Ok with a JSON response (name of the Playlist and Array of IDs)
//    *         or a 400 Bad Request if the request couldn't be parsed
//    */
//  def generatePlaylist = Action(parse.json) { implicit request =>
//    JSONParser.parseRequest(request.body) match {
//        // the request is not parsed correctly: return an HTTP 400 Bad Request
//      case None => BadRequest("Json Request failed")
//      case Some(p) =>
//        val db = getFromRedisThenMongo(p)
//        // call the playlist generation algorithm with (MusicCollection, Set[Constraint], Int) args
//        val playlist = GA.generatePlaylist(db, p.constraints, p.length)
//        // the JSON response with the playlist name and the returned playlist
//        val js = createJsonResponse(p.name, playlist)
//        Ok(js)
//    }
//  }
//
//  def getFromMongo(p: PlaylistRequest): MusicCollection = {
//    new MusicCollection(
//      p.ids.flatMap(id => MongoController.readByID(dbTracks, id))
//    )
//  }
//
//  def getFromRedisThenMongo(p: PlaylistRequest): MusicCollection = {
//   new MusicCollection(
//     for(id <- p.ids) yield {
////       cache.get[Song](id) match {
////         case Some(song) => song
////         case None => {
//           val song = MongoController.readByID(dbTracks, id).get
////           cache.set(id, song)
//           song
//         })
////       }
//     }
//
//  /**
//    * The JSON response to send back to the user
//    *
//    * @param name the name of the playlist
//    * @param playlist the Playlist generated
//    * @return a JSON with the name of the playlist and an array of tracks ids
//    */
//  def createJsonResponse(name: String, playlist: Playlist): JsValue = {
//    // map each Song in the playlist back to only its id
//    // as the view is supposed to reconstruct the response
//    val tracksID = Json.toJson(playlist.songs.map(s => s.id))
//    Json.obj(
//      "name" -> name, // a String name of the playlist
//      "ids" -> tracksID // an Array with the tracks IDs
//    )
//  }
//
//}