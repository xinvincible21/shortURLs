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
class AsyncController @Inject() (actorSystem: ActorSystem,cache:CacheApi, db: DBApi)(implicit exec: ExecutionContext) extends Controller {
  /**
   * Create an Action that returns a plain text message after a delay
   * of 1 second.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/message`.
   */

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

  def shorten(url: String) = Action.async{
    val defaultDB = new DefaultDB(db)
    // check URL exists
    // if true
    // return URL
    // else create new ShortURL add to cache

    val future = Future {
      val shortURL =
        cache.getOrElse(url) {
          val shortURL = createURL(url = url)
          defaultDB.insertURL(shortURL)
          cache.set(url, shortURL)
          shortURL
        }
      shortURL
    }
    println(cache)
    future.map(m => Ok(Json.toJson(m)))
  }

}
