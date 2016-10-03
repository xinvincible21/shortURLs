package utils

import java.sql.Statement._

import play.api.Logger
import play.api.libs.json._
import models.ShortURL
import play.api.db.{DB, DBApi, Database}
import play.api.Play.current

class DefaultDB(db:DBApi) {

//  def test() = DB.withConnection { implicit conn =>
//    val stmt = conn.createStatement()
//    val rs = stmt.executeQuery("select id from short_utls where id = 1")
//    rs.next()
//    println(rs.getString(""))
//  }
//

  def insertURL(data:ShortURL) = db.database("default").withConnection { conn =>
    val table = "short_urls"
    val fields = "url, short_url"

    val duplicateKeyUpdate = " on duplicate key update url = VALUES(url), short_url = VALUES(short_url)"

    val upsert = s"insert into $table ( $fields ) VALUES ( '${data.url}', '${data.shortURL}' ) $duplicateKeyUpdate;"
    Logger.debug(upsert)
    val stmt = conn.prepareStatement(upsert)
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



}