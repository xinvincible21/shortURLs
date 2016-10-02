package models

import play.api.libs.json.Json


object ShortURL {
  implicit val shortURL = Json.format[ShortURL]
}

case class ShortURL(url:String, shortURL:String)
