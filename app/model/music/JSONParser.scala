package model.music

import com.fasterxml.jackson.annotation.JsonValue

import scala.reflect.runtime.{universe => ru}
import com.wrapper.spotify.models.{AudioFeature, Track}
import model.constraints._
import play.api.libs.json._

import scala.runtime.RichInt
/**
  *
  */
object JSONParser {

  def parseLength(js: JsValue): Int = (js \ "length").as[Int]

  def parseName(js: JsValue): String = (js \ "name").asOpt[String].getOrElse("")

  def parseIDS(js: JsValue): Vector[String] = {
    val ids = (js \ "ids").asOpt[Array[String]].getOrElse(Array())
    ids.toVector
  }

  def parseConstraints(js: JsValue): Set[Constraint] = {
    val constraints = js \ "constraints"
    if (constraints.isInstanceOf[JsUndefined]) Set()
    (constraints \\ "constraint").map(c => {
      // TODO should create key: 'constraint-type' to determine constructor args
      // i.e. type: { MonotonicConstraint | UnaryConstraint }
      val cls = Class.forName("model.constraints." + (c \ "name").as[String])
      val args = getBoxedArgs(c)
      instantiate(cls)(args._1, args._2, args._3).asInstanceOf[Constraint]
    }).toSet
  }

  /**
    * @see http://stackoverflow.com/a/1642012
    * @param cls
    * @param args
    * @return
    */
  private def instantiate(cls: java.lang.Class[_])(args: AnyRef*): AnyRef = {
    val constructor = cls.getConstructors()(0)
    constructor.newInstance(args: _*).asInstanceOf[AnyRef]
  }

  private def getBoxedArgs(js: JsValue) = {
    (getInt(js, "from").asInstanceOf[AnyRef],
      getInt(js, "to").asInstanceOf[AnyRef],
      parseAttribute(js))
  }

  private def getInt(js: JsValue, key: String): Int = (js \ key).as[String].toInt

  private def parseAttribute(js: JsValue): Attribute = {
    val attrName = (js \ "attribute" \ "name").as[String]
    val value = (js \ "attribute" \ "value").asOpt[String].getOrElse("")
    MusicUtil.extractAttribute((attrName, value))
  }

}