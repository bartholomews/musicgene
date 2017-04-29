package controllers.wrapper.entities

import play.api.libs.json
import play.api.libs.json.{JsPath, JsSuccess, Reads}

case class SeqAudioFeatures
(audio_features: List[Option[AudioFeatures]])