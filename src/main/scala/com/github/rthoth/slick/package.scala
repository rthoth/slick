package com.github.rthoth

import org.reactivestreams.Publisher

import scala.language.implicitConversions
import _root_.slick.dbio.{Effect, DBIOAction, NoStream}

package object slick {

  implicit def publisherToAction[E <: Effect](publisher: Publisher[_ <: DBIOAction[_, NoStream, E]]): DBIOAction[Unit, NoStream, E] = {
    new PublisherDBIOAction(publisher)
  }

}
