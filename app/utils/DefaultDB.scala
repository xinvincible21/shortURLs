package utils

import play.api.Logger
import models.ShortURL
import play.api.db.{DB, DBApi, Database}

class DefaultDB(db:DBApi) {

  def findNextID() = db.database("default").withConnection { conn =>
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("select last_insert_id() as id from short_urls")

    val id =
      Iterator.continually ((rs, rs.next)).takeWhile (_._2).map (_._1).map {result =>
          result.getLong("id")
      }
      id.toList.headOption match {
        case Some(autoincrement) => autoincrement + 1
        case None => 1
      }
  }

  def insertURL(data:ShortURL) = db.database("default").withConnection { conn =>
    val table = "short_urls"
    val fields = "url, short_url"
    val insert = s"insert into $table ( $fields ) VALUES (?, ?) "
    val stmt = conn.prepareStatement(insert)
    stmt.setString(1, data.url)
    stmt.setString(2, data.shortURL)
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
    val q = s"select url from short_urls where short_url = ?"
    val stmt = conn.prepareStatement(q)
    stmt.setString(1, shortURL)
    Logger.debug(q)
    Logger.debug(shortURL)
    val rs = stmt.executeQuery()

    val urls =
      Iterator.continually ((rs, rs.next)).takeWhile (_._2).map (_._1).map {result =>
          result.getString("url")
      }
    urls.toList.headOption
  }

}