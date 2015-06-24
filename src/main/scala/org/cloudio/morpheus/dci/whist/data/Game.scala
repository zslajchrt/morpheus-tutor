package org.cloudio.morpheus.dci.whist.data

/**
 * Created by zslajchrt on 24/06/15.
 */
trait Game {
  def rounds: Int
  def group: List[Person]
}
