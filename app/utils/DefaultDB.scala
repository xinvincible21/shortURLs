package utils

import play.api.Logger
import models.ShortURL
import play.api.db.{DB, DBApi, Database}

class DefaultDB(db:DBApi) {

  def insertURL(data:ShortURL) = db.database("default").withConnection { conn =>
    val table = "short_urls"
    val fields = "url, short_url"
    val insert = s"insert into $table ( $fields ) VALUES ( '${data.url}', '${data.shortURL}' ) ;"
    Logger.debug(insert)
    val stmt = conn.prepareStatement(insert)
    stmt.executeUpdate()
  }

  def findShortURLs() = db.database("default").withConnection { conn =>
    val stmt = conn.createStatement()
    val q = s"select * from short_urls"
    Logger.debug(q)
    val rs = stmt.executeQuery(q)

    val urls =
      Iterator.continually ((rs, rs.next)).takeWhile (_._2).map (_._1).map {result =>
        ShortURL(
          url = result.getString("url"),
          shortURL = result.getString("short_url")
        )
      }
    urls.toList
  }

  def findLongURL(shortURL:String) = db.database("default").withConnection { conn =>
    val stmt = conn.createStatement()
    val q = s"select url from short_urls where short_url = '$shortURL'"
    Logger.debug(q)
    val rs = stmt.executeQuery(q)

    val urls =
      Iterator.continually ((rs, rs.next)).takeWhile (_._2).map (_._1).map {result =>
          result.getString("url")
      }
    urls.toList.headOption
  }

}