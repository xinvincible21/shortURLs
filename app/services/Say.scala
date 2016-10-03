package services

import javax.inject._

import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import play.api.db.DBApi
import utils.DefaultDB
import play.api.cache.CacheApi

trait Say {
  def hello(): Unit
  def goodbye(): Unit
}

@Singleton
class SayImpl @Inject() (appLifecycle: ApplicationLifecycle, cache:CacheApi, db:DBApi) extends Say {
  override def hello(): Unit = println("Hello!")
  override def goodbye(): Unit = println("Goodbye!")

  // You can do this, or just explicitly call `hello()` at the end
  def start(): Unit = {
    val defaultDB = new DefaultDB(db)
    val shortURLs = defaultDB.findShortURLs()

    for(shortURL <- shortURLs){
      Logger.info(s"inserting url key ${shortURL.url} value $shortURL into cache.")
      cache.set(shortURL.url, shortURL)
      Logger.info(s"inserting url key ${shortURL.shortURL} value $shortURL into cache.")
      cache.set(shortURL.shortURL, shortURL)

    }

  }

  // When the application starts, register a stop hook with the
  // ApplicationLifecycle object. The code inside the stop hook will
  // be run when the application stops.
  appLifecycle.addStopHook { () =>
    goodbye()
    Future.successful(())
  }

  // Called when this singleton is constructed (could be replaced by `hello()`)
  start()
}