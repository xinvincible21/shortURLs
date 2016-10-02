package controllers

import akka.actor.ActorSystem
import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import play.api.cache._
import play.api.mvc._
import javax.inject.Inject
import scala.util.Random
import play.api.libs.json._
import models.ShortURL
import models.ShortURL._

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
class AsyncController @Inject() (actorSystem: ActorSystem,cache:CacheApi) (implicit exec: ExecutionContext) extends Controller {
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

  def shorten(url: String) = Action.async{
    val future = scala.concurrent.Future {
      val random = Random.alphanumeric.take(7).mkString
      val shortURL = ShortURL(url = url, shortURL = random)
      println(shortURL)
      Json.toJson(shortURL)
    }
    future.map(m => Ok(m))
  }

}
