package controllers

import javax.inject._

import play.api.db.DBApi

import scala.concurrent.{ExecutionContext, Future}
import play.api.cache._
import play.api.mvc._

import scala.util.Random
import play.api.libs.json._
import models.ShortURL
import models.ShortURL._
import javax.inject.Inject

import utils.DefaultDB
import utils.Config.baseURL
import play.api.Logger

@Singleton
class AsyncController @Inject() (cache:CacheApi, db: DBApi)(implicit exec: ExecutionContext) extends Controller{
  val clickTracker = Logger("click_tracker")
  val applicationLogger = Logger("application")

  def countClicks(hash:String) = Action.async{ request =>
    val url = s"$baseURL/$hash"
    val urlNoTrailingSlash = removeTrailingSlash(url = url)
    clickTracker.info(s"$urlNoTrailingSlash|1")

    val future =
    cache.get[ShortURL](urlNoTrailingSlash) match {
      case Some(cachedURL) => Future(Redirect(cachedURL.url))
      case None =>
        val defaultDB = new DefaultDB(db)
        defaultDB.findLongURL(shortURL = urlNoTrailingSlash) match {
          case Some(dbURL) =>
            val shortURL = ShortURL(url = urlNoTrailingSlash, shortURL = dbURL)
            applicationLogger.logger.info(s"inserting url key $urlNoTrailingSlash value $shortURL into cache.")
            cache.set(urlNoTrailingSlash, shortURL)
            applicationLogger.info(s"inserting url key $dbURL value $shortURL into cache.")
            cache.set(dbURL, shortURL)
            Future(Redirect(dbURL))
          case None => Future(Ok(Json.toJson(s"url $url not found.")))
        }
    }
    future
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createURL(url:String) = {
    val random = Random.alphanumeric.take(7).mkString
    val shortURL = ShortURL(url = url, shortURL = s"$baseURL/$random")
    shortURL
  }

  def removeTrailingSlash(url:String) = {
    url.endsWith("/") match {
      case true  => url.substring(0, url.length() - 1)
      case false => url
    }
  }

  def shorten(url: String) = Action.async{
    val urlNoTrailingSlash = removeTrailingSlash(url = url)
    val defaultDB = new DefaultDB(db)

    val future = Future {
        cache.get[ShortURL](urlNoTrailingSlash) match {
          case Some(cachedURL) =>
            applicationLogger.info(s"key $urlNoTrailingSlash value $cachedURL retrieved from cache.")
            cachedURL
          case None =>
            val shortURL = createURL(url = urlNoTrailingSlash)
            defaultDB.insertURL(shortURL)
            applicationLogger.logger.info(s"inserting url key $urlNoTrailingSlash value $shortURL into cache.")
            cache.set(urlNoTrailingSlash, shortURL)
            applicationLogger.info(s"inserting url key ${shortURL.shortURL} value $shortURL into cache.")
            cache.set(shortURL.shortURL, shortURL)
            shortURL
        }
    }
    future.map(r => Ok(Json.toJson(r)))
  }

  def lengthen(url:String) = Action.async {
    val defaultDB = new DefaultDB(db)

    val urlNoTrailingSlash = removeTrailingSlash(url = url)

    val optLongURL =
    cache.get[ShortURL](urlNoTrailingSlash) match {
      case Some(cachedURL) =>
        applicationLogger.info(s"key $urlNoTrailingSlash value $cachedURL retrieved from cache.")
        Some(cachedURL.url)
      case None =>
        defaultDB.findLongURL(urlNoTrailingSlash) match {
          case Some(dbURL) =>
            val shortURL = ShortURL(url = dbURL, shortURL = urlNoTrailingSlash)
            applicationLogger.info(s"inserting url key $urlNoTrailingSlash value $shortURL into cache.")
            cache.set(urlNoTrailingSlash, shortURL)
            applicationLogger.info(s"inserting url key ${shortURL.shortURL} value $shortURL into cache.")
            cache.set(shortURL.shortURL, shortURL)
            Option(dbURL)
          case None => None
        }
      }

    val future = Future{optLongURL}
      future.map {
        case Some(longURL) => Ok(Json.toJson(longURL))
        case None => BadRequest(Json.toJson(s"url $url not found."))
      }
  }
}
