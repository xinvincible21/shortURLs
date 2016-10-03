package services

import javax.inject._

import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import play.api.db.DBApi
import utils.DefaultDB
import play.api.cache.CacheApi


trait LoadCache {
  def start()
}

@Singleton
class LoadCacheImpl @Inject()(appLifecycle: ApplicationLifecycle, cache:CacheApi, db:DBApi) extends LoadCache{
  
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
  
  // Called when this singleton is constructed (could be replaced by `hello()`)
  start()
}