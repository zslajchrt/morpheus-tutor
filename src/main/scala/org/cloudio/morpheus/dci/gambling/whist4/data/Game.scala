package org.cloudio.morpheus.dci.gambling.whist4.data

/**
 * Created by zslajchrt on 24/06/15.
 */
trait Game {
  def rounds: Int
  def group: List[Person]
}
