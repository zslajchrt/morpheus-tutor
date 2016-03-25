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


trait AccountInit {
  val initialBalance: BigDecimal
}

case class AccountInitData(initialBalance: BigDecimal) extends AccountInit

@fragment
trait AccountBase extends Account with dlg[AccountInit] {

  private var bal: BigDecimal = initialBalance

  def balance = bal

  def decreaseBalance(amount: BigDecimal) = bal -= amount

  def increaseBalance(amount: BigDecimal) = bal += amount
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