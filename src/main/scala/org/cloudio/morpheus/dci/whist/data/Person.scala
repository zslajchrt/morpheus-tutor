package org.cloudio.morpheus.dci.whist.data

import org.morpheus.{dimension, fragment}

/**
 * Created by zslajchrt on 23/06/15.
 */
@dimension
trait Person {
  def name: String
  def score: Int
}


@fragment
trait MutablePerson extends Person {
  var name: String = ""
  var score: Int = 0
}
