package models.services

import scala.concurrent.duration._
import scala.util.Try
import rx.lang.scala.Observable
import rx.lang.scala.subscriptions.Subscription
import scala.util.Success
import scala.util.Failure
import scala.Some
import play.api.Play
import play.api.Play.current
import models.mongo.{Users, Branches, Collection}
import com.mongodb.casbah.commons.MongoDBObject

object CacheService {
  def cache[T](interval: Duration, collection: Collection[T])(getValues: => List[T]) = {
    Observable.interval(interval).map(tick => Try {
      getValues
    })
      .subscribe(tryResult => tryResult match {
      case Success(data) =>
        println(s"saving to mongo ${data.length}")
        collection.findAll.foreach(collection.remove)
        data.foreach(collection.save)
      case Failure(e) => play.Logger.error("Error", e)
    })
  }

  val user = Play.configuration.getString("cache.user").get
  val pullRequestInterval = Play.configuration.getInt("cache.interval.pullRequests").getOrElse(30).seconds
  val githubBranchesInterval = Play.configuration.getInt("cache.interval.githubBranches").getOrElse(10).seconds

  def start = {
    Users.findOneByUsername(user) match {
      case Some(u) =>
        implicit val user = u
        val repo = new BranchesService()

        //        val sub2 = cache[Branch](githubBranchesInterval, Branches) {
        //          watch("cache: get github branches") {
        //            repo.getBranches
        //          }
        //        }

        val sub2 = {
          Observable.interval(githubBranchesInterval).map(tick => Try {
            repo.getBranches
          })
            .subscribe(tryResult => tryResult match {
            case Success(data) => data.foreach(branch => Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = true, multi = false, Branches.dao.collection.writeConcern))
            case Failure(e) => play.Logger.error("Error", e)
          })
        }

        Subscription {
          sub2.unsubscribe()
        }

      case None => play.Logger.error(s"Could not find user $user for cache service"); Subscription {}
    }
  }

}
