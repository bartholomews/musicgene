package controllers.http

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, JsonNaming}

// https://www.playframework.com/documentation/latest/ScalaJsonAutomated
package object codecs {
  // TODO: test if this works, shouldn't need the manual key conversions e.g. in `nonRefreshableTokenReads`
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  val withDiscriminator: Json.WithOptions[Json.MacroOptions] = {
    Json.configured(
      JsonConfiguration(
        discriminator = "type",
        typeNaming = JsonNaming { fullyQualifiedName =>
          val fr: String = fullyQualifiedName.split("\\.").last
          val tail = fr.tail.foldLeft("") { (acc, curr) =>
            if (curr.isUpper) s"${acc}_${curr.toLower}" else s"$acc$curr"
          }
          s"${fr.head.toLower}$tail"
        }
      )
    )
  }
}
