package com.github.rthoth.slick

import slick.jdbc.H2Profile.api._

object Person {

  val Table = TableQuery[Persons]
}

case class Person(name: String, age: Int) extends Serializable

class Persons(tag: Tag) extends Table[Person](tag, "person") {

  override def * = (name, age) <> ((Person.apply _).tupled, Person.unapply)

  def age = column[Int]("age")

  def name = column[String]("name")
}
