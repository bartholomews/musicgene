package utils

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object ConversionUtils {

  def encode(str: String): String = {
    str.replace("{", "%7B").replace("}", "%7D")
  }

}
