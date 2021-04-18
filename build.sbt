ThisBuild / organization := "com.github.rthoth"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "slick-reactivestreams"
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "org.scalatest" %% "scalatest" % "3.2.6" % "test",
      "com.h2database" % "h2" % "1.4.200" % "test",
      "io.reactivex.rxjava3" % "rxjava" % "3.0.13-RC2" % "test"
    )
  )