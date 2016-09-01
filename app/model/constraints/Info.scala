package model.constraints

import model.music.Attribute

/**
  *
  * @param attr
  * @param index
  * @param distance
  */
case class Info(attr: Attribute, index: Int, distance: Double = 0.0)
