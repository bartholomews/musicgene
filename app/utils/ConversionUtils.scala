package utils

object ConversionUtils {

  def encode(str: String): String = {
    str.replace("{", "%7B").replace("}", "%7D")
  }

}
