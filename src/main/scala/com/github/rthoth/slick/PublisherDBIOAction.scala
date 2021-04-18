package com.github.rthoth.slick

import org.reactivestreams.{Publisher, Subscriber, Subscription}
import slick.SlickException
import slick.basic.BasicBackend
import slick.dbio.{DBIOAction, Effect, NoStream, SynchronousDatabaseAction}
import slick.util.DumpInfo

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

class PublisherDBIOAction[E <: Effect, A <: DBIOAction[_, NoStream, E]](publisher: Publisher[A]) extends SynchronousDatabaseAction[Unit, NoStream, BasicBackend, E] {

  override def getDumpInfo: DumpInfo = DumpInfo(
    name = "Publisher Database Action"
  )

  override def run(context: BasicBackend#Context): Unit = {

    val promise = Promise[Unit]()
    var subscription: Subscription = null

    publisher.subscribe(new Subscriber[A] {

      override def onSubscribe(s: Subscription): Unit = {
        subscription = s
        subscription.request(1)
      }

      override def onNext(a: A): Unit = {
        a match {
          case _: SynchronousDatabaseAction[_, _, _, _] =>
            a.asInstanceOf[SynchronousDatabaseAction[_, NoStream, BasicBackend, _]].run(context)
            subscription.request(1)
          case _ =>
            throw new SlickException(s"Unsupported stream action $a for $this!")
        }
      }

      override def onError(cause: Throwable): Unit = {
        promise.failure(cause)
      }

      override def onComplete(): Unit = {
        promise.success(())
      }
    })

    Await.result(promise.future, Duration.Inf)
  }
}
