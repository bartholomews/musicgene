package util

import model.music._
import play.api.libs.json._

/**
  *
  */
object JsonUtil extends App {

  def toJson(songs: Seq[Song]): Seq[JsValue] = songs.map(s => toJson(s))

  def toJson(song: Song): JsValue = JsObject(Seq(
    "spotify_id" -> JsString(song.id),
    "attributes" -> JsObject(toJsonAttribute(song.attributes))
  ))

  /**
    * @param attr
    * @return
    */
  private def toJsonAttribute(attr: Set[Attribute]): Map[String, JsValue] = {
    attr.map(a => {
      val name = a.getClass.getSimpleName
      a match {
        case _:AudioAttribute => name -> JsNumber(a.value.asInstanceOf[Double])
        case _:TimeAttribute => name -> JsNumber(a.value.asInstanceOf[Int])
        case _:TextAttribute => name -> JsString(a.value.asInstanceOf[String])
        case x => throw new Exception(x + ": Attribute value type is unknown")
      }
      // @see http://stackoverflow.com/a/2925643
    })(collection.breakOut)
  }

}
