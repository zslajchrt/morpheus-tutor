package org.cloudio.morpheus.dci.moneyTransfer.data

import org.morpheus.Morpheus.dlg
import org.morpheus._

/**
 * Created by zslajchrt on 22/06/15.
 */


@dimension
trait Account {
  def balance: BigDecimal

  def decreaseBalance(amount: BigDecimal): Unit

  def increaseBalance(amount: BigDecimal): Unit
}


@fragment
trait AccountBase extends Account {

  private var bal: BigDecimal = 0

  def balance = bal

  def decreaseBalance(amount: BigDecimal) = bal -= amount

  def increaseBalance(amount: BigDecimal) = bal += amount
}

@dimension @wrapper
trait BlockedAccount extends Account {

  override def decreaseBalance(amount: BigDecimal): Unit = {
    sys.error("Blocked")
  }

}

// various accounts

@fragment
trait SavingsAccount {
  this: AccountBase =>
}

@fragment
trait CheckingAccount {
  this: AccountBase =>
}

@fragment
trait RetiringAccount {
  this: AccountBase =>
}