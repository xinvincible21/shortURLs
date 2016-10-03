package utils

import java.sql.Connection

import com.typesafe.config.ConfigFactory
//import com.zaxxer.hikari.HikariDataSource
//import com.zaxxer.hikari.pool.HikariPool

object Config {
  val config = ConfigFactory.load()

  val baseURL = config.getString("base.url")
//  val db = new HikariDataSource()
//  db.setRegisterMbeans(true)
//  db.setPoolName("short-url")
//  db.setMaximumPoolSize(config.getInt("db.default.maxPoolSize"))
//  db.setMinimumIdle(config.getInt("db.default.minPoolSize"))
//  db.setDataSourceClassName(config.getString("db.default.driver"))
//  db.addDataSourceProperty("serverName", config.getString("db.default.host"))
//  db.addDataSourceProperty("databaseName", config.getString("db.default.schema"))
//  db.addDataSourceProperty("user", config.getString("db.default.username"))
//  db.addDataSourceProperty("password", config.getString("db.default.password"))
//
//  val pool = new HikariPool(db)

//  object DB {
//    def withConnection[T](f: Connection => T) = {
//      val c = pool.getConnection
//      try {
//        f(c)
//      } finally {
//        c.close()
//      }
//    }
//  }
}