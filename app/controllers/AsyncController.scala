package controllers

import akka.actor.ActorSystem
import javax.inject._

import play.api.db.DB
import play.api.db.Database
import play.api.Play.current
import play.api.db.DBApi

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import play.api.cache._
import play.api.mvc._
import javax.inject.Inject

import scala.util.Random
import play.api.libs.json._
import models.ShortURL
import models.ShortURL._
import javax.inject.Inject

import utils.DefaultDB
import utils.Config.baseURL
import play.api.Logger


/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
 * run code after a delay.
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.
 */
@Singleton
class AsyncController @Inject() (actorSystem: ActorSystem,cache:CacheApi, db: DBApi)(implicit exec: ExecutionContext) extends Controller{
  val clickTracker = Logger("click_tracker")
  /**
   * Create an Action that returns a plain text message after a delay
   * of 1 second.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/message`.
   */

  def countClicks(hash:String) = Action.async{ request =>
    val url = s"$baseURL/$hash"
    println(s"$url clicked")
    val urlNoTrailingSlash = removeTrailingSlash(url = url)
    clickTracker.info(s"$urlNoTrailingSlash,1")

    val future =
    cache.get[ShortURL](urlNoTrailingSlash) match {
      case Some(cachedURL) => Future(Redirect(cachedURL.url))
      case None =>
        val defaultDB = new DefaultDB(db)
        defaultDB.findLongURL(shortURL = urlNoTrailingSlash) match {
          case Some(dbURL) => Future(Redirect(dbURL))
          case None => Future(Ok(Json.toJson(s"url $url not found.")))
        }
    }
    future
  }

  def message = Action.async {
    getFutureMessage(1.second).map { msg => Ok(msg) }
  }

  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) { promise.success("Hi!") }
    promise.future
  }

  def createURL(url:String) = {
    val random = Random.alphanumeric.take(7).mkString
    val shortURL = ShortURL(url = url, shortURL = s"$baseURL/$random")
    println(shortURL)
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
    // check URL exists
    // if true
    // return URL
    // else create new ShortURL add to cache

    val future = Future {
        cache.get[ShortURL](urlNoTrailingSlash) match {
          case Some(cachedURL) =>
            Logger.info(s"key $urlNoTrailingSlash value $cachedURL retrieved from cache.")
            cachedURL
          case None =>
            val shortURL = createURL(url = urlNoTrailingSlash)
            defaultDB.insertURL(shortURL)
            Logger.info(s"inserting url key $urlNoTrailingSlash value $shortURL into cache.")
            cache.set(urlNoTrailingSlash, shortURL)
            Logger.info(s"inserting url key ${shortURL.shortURL} value $shortURL into cache.")
            cache.set(shortURL.shortURL, shortURL)
            shortURL
        }
    }
    println(cache)
    future.map(r => Ok(Json.toJson(r)))
  }

  def lengthen(url:String) = Action.async {
    val defaultDB = new DefaultDB(db)

    val urlNoTrailingSlash = removeTrailingSlash(url = url)

    val test =
    cache.get[ShortURL](urlNoTrailingSlash) match {
      case Some(cachedURL) =>
        Logger.info(s"key $urlNoTrailingSlash value $cachedURL retrieved from cache.")
        Some(cachedURL.url)
      case None => defaultDB.findLongURL(urlNoTrailingSlash)
      }

    val future = Future{test}
      future.map {
        case Some(url) => Ok(Json.toJson(url))
        case None => BadRequest(Json.toJson(s"url $url not found."))
      }
  }
}
