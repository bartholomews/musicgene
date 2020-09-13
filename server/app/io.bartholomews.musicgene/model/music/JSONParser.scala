//package model.music
//
//import model.constraints._
//import play.api.libs.json._
//
//
//object JSONParser {
//
//  /**
//    * Parse a JSON Playlist request
//    * @return Some(PlaylistRequest) if parsing is successfull,
//    *         None otherwise
//    *
//    *         TODO: use a play Reader!
//    */
//  def parseRequest(js: JsValue): Option[PlaylistRequest] = {
//    try {
//      Some(PlaylistRequest(
//        parseName(js),
//        parseLength(js),
//        parseIDS(js),
//        parseConstraints(js)
//      ))
//    }
//    catch {
//      case x: Throwable => None
//    }
//  }
//
//  /**
//    * Retrieve the name of the Playlist
//    */
//  def parseName(js: JsValue): String = (js \ "name").asOpt[String].getOrElse("")
//
//  /**
//    * Retrieve the length of the Playlist
//   */
//  def parseLength(js: JsValue): Int = (js \ "length").as[Int]
//
//  /**
//    * Retrieve the IDs of the tracks which make up the Playlist
//    */
//  def parseIDS(js: JsValue): Vector[String] = {
//    val ids = (js \ "ids").asOpt[Array[String]].getOrElse(Array())
//    ids.toVector
//  }
//
//  /**
//    * Retrieve the Set(Constraint) set by the request.
//    * A Constraint is instantiated with reflection
//    * checking the key of JSON request against class names
//    * in package model.constraints
//    * and construct an instance based on its constructor 'type'
//    */
//  def parseConstraints(js: JsValue): Set[Constraint] = {
//    val constraints = js \ "constraints"
//    if (constraints.isInstanceOf[JsUndefined]) Set()
//    else (constraints \\ "constraint").map(c => {
//      val cls = Class.forName("model.constraints." + (c \ "name").as[String])
//      (c \ "type").as[String] match {
//        case "RangeConstraint" =>
//          val args = (
//            getInt(c, "from"),
//            getInt(c, "to"),
//            getInt(c, "min"),
//            getInt(c, "max"),
//            parseAttribute(c)
//            )
//          instantiate(cls)(args._1, args._2, args._3, args._4, args._5)
//        case "IndexedConstraint" =>
//          val args = (
//            getInt(c, "from"),
//            getInt(c, "to"),
//            parseAttribute(c)
//            )
//          instantiate(cls)(args._1, args._2, args._3)
//        case "Constraint" => instantiate(cls)(parseAttribute(js))
//        case x => throw new Exception("Constraint type not supported: " + x)
//      }
//    }).toSet
//  }
//
//  /**
//    * @see http://stackoverflow.com/a/1642012
//    */
//  def instantiate(cls: java.lang.Class[_])(args: AnyRef*): Constraint = {
//    val constructor = cls.getConstructors()(0)
//    constructor.newInstance(args: _*).asInstanceOf[Constraint]
//  }
//
//  def getInt(js: JsValue, key: String): AnyRef = (js \ key).as[String].toInt.asInstanceOf[AnyRef]
//
//  def parseAttribute(js: JsValue): Attribute = {
//    val attrName = (js \ "attribute" \ "name").as[String]
//    val value = (js \ "attribute" \ "value").asOpt[String].getOrElse("")
//    MusicUtil.extractAttribute((attrName, value))
//  }
//
//}