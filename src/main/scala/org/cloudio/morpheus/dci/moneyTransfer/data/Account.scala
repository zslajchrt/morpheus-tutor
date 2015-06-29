package org.cloudio.morpheus.dci.moneyTransfer.data

import org.morpheus._

/**
 * Created by zslajchrt on 22/06/15.
 */


//case class AccountInit(initialBalance: BigDecimal) extends AccountData {
//  override var balance: BigDecimal = initialBalance
//}

trait Account {
  def Balance: BigDecimal

  def decreaseBalance(amount: BigDecimal): Unit

  def increaseBalance(amount: BigDecimal): Unit
}


class AccountImpl(initialBalance: BigDecimal) extends Account {

  private var balance: BigDecimal = initialBalance

  def Balance = balance

  def decreaseBalance(amount: BigDecimal) = balance -= amount

  def increaseBalance(amount: BigDecimal) = balance += amount
}
