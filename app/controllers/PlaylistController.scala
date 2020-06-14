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

//
//}