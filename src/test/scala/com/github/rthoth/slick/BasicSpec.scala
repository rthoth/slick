package com.github.rthoth.slick

import io.reactivex.rxjava3.core.Flowable
import org.reactivestreams.Publisher
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits._

class BasicSpec extends Spec {

  "A simple table." in {
    val db = createDB()
    val publisher: Publisher[DBIOAction[_, NoStream, Effect]] = Flowable.fromArray(
      sqlu"CREATE TABLE simple (id bigint, name varchar(255));",
      sqlu"""INSERT INTO simple VALUES (20,'Hello');""",
      sqlu"""INSERT INTO simple VALUES (20,'World!');"""
    )

    wait(db.run(publisherToAction(publisher).transactionally))
    db.close()
  }

  "A mapped case class with transaction." in {

    val create = Flowable.fromArray(
      Person.Table.schema.create.asInstanceOf[DBIOAction[Unit, NoStream, Effect]]
    )

    val persons = (for (i <- 0 until 100) yield Person(s"P$i", i)).toArray
    val insert = Flowable
      .fromArray(persons: _*)
      .map(person => (Person.Table += person).asInstanceOf[DBIOAction[Unit, NoStream, Effect]])

    val delete = Flowable
      .fromArray(
        Person.Table.filter(_.age < 50).delete.asInstanceOf[DBIOAction[Unit, NoStream, Effect]]
      )

    val db = createDB()
    val flowable = create.concatWith(insert).concatWith(delete)
    wait(db.run(publisherToAction(flowable).transactionally))
    wait(db.run(Person.Table.result)) should have length 50
    db.close()
  }

  "When something goes wrong!" in {
    val db = createDB()
    db.run(DBIO.seq(Person.Table.schema.create))

    val insert = Flowable
      .fromArray(
        (for (i <- 0 until 100) yield Person(s"Person $i", i)): _*
      )
      .map(person => {
        if (person.age != 50)
          Person.Table += person
        else
          throw new IllegalStateException("Ops!")
      })

    wait(db.run(publisherToAction(insert).transactionally).recover({ case e: IllegalStateException if e.getMessage == "Ops!" => () }))
    wait(db.run(Person.Table.result)) shouldBe empty
    db.close()
  }

  "Parallel update." in {
    val db = createDB()
    wait(db.run(Person.Table.schema.create))
    wait(db.run(Person.Table ++= (for (i <- 0 until 100) yield Person(s"Person $i", age = i))))

    val flowable = Flowable
      .fromPublisher(db.stream(Person.Table.result))
      .parallel()
      .map(person => Person.Table.filter(_.age === person.age).map(_.name).update(person.name + "!!"))
      .sequential()

    wait(db.run(publisherToAction(flowable).transactionally))
    db.close()
  }
}
