package io.bartholomews.musicgene.model.constraints

import io.bartholomews.musicgene.model.music.AudioFeature

/**
  * Potential information about a `Score` result
  *
  * @param attr the Attribute which is tested on that Score
  * @param index the index of the song tested
  * @param distance the distance result of the evaluation, default at 0.0
  *                 for constraints which do not reason in terms of distance
  */
case class Info[A](attr: AudioFeature, index: Int, distance: Double = 0.0)

