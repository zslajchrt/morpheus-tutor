package org.cloudio.morpheus.dci.moneyTransfer.uses.moneyTransfer

import org.cloudio.morpheus.dci.moneyTransfer.data._

import org.morpheus.Morpheus._
import org.morpheus._
import org.morpheus.dci.DCI._

/**
 * Created by zslajchrt on 22/06/15.
 */

@fragment
trait Source {
  this: Account =>

  private def withdraw(amount: BigDecimal) {
    decreaseBalance(amount)
  }

  def transfer(destination: Destination with Account, amount: BigDecimal): Unit = {
    destination.deposit(amount)
    withdraw(amount)
  }

}

@fragment
trait Destination {
  this: Account =>

  def deposit(amount: BigDecimal): Unit = {
    increaseBalance(amount)
  }
}

class Context(srcAcc: &[$[Source] with Account], dstAcc: &[$[Destination] with Account], val amount: BigDecimal) {

  val source = *(srcAcc)
  val destination = *(dstAcc)

  def trans(): Unit = {
    source.!.transfer(destination.!, amount)
  }

}

object App {

  def main(args: Array[String]): Unit = {
    val savingsAcc = {
      implicit val accBaseFactory = single[AccountBase, AccountInit](AccountInitData(10))
      singleton[AccountBase with SavingsAccount].!
    }

    val checkingAcc = {
      implicit val accBaseFactory = single[AccountBase, AccountInit](AccountInitData(50))
      singleton[AccountBase with CheckingAccount].!
    }

    println(s"Source balance is: ${savingsAcc.balance}")
    println(s"Destination balance is: ${checkingAcc.balance}")

    val ctx = new Context(savingsAcc, checkingAcc, 5)
    ctx.trans()

    println(s"Source balance is now: ${savingsAcc.balance}")
    println(s"Destination balance is now: ${checkingAcc.balance}")
  }

}