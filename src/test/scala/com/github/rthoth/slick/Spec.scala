package com.github.rthoth.slick

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Random
import scala.util.control.NonFatal

abstract class Spec extends AnyFreeSpec with Matchers {

  protected def createDB(): Database = {
    val db = Database.forURL(s"jdbc:h2:mem:Database${BigInt(Random.nextBytes(4)).abs.toString(32)}", driver = "org.h2.Driver", keepAliveConnection = true)

    try {
      Await.result(db.run(sql"select 1".as[Int]), Duration.Inf)
    } catch {
      case NonFatal(_) =>
    }

    db
  }

  protected def wait[T](future: Future[T]): T = Await.result(future, Duration.Inf)
}
