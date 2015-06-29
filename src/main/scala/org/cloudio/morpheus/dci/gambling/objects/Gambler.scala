package org.cloudio.morpheus.dci.gambling.objects

/**
 * Created by zslajchrt on 28/06/15.
 */
trait Gambler {
  def name: String
  def wins: Int
  def losses: Int
  def increaseWins(win: BigDecimal)
  def increaseLosses(loss: BigDecimal)
  def balance: BigDecimal
}
