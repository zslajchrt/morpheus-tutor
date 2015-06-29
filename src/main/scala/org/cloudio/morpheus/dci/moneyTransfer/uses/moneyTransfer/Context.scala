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
  this: Account with Context =>

  private def withdraw(amount: BigDecimal) {
    decreaseBalance(amount)
  }

  def transfer {
    Console.println("Source balance is: " + Balance)
    Console.println("Destination balance is: " + Destination.Balance)

    Destination.deposit(Amount)
    withdraw(Amount)

    Console.println("Source balance is now: " + Balance)
    Console.println("Destination balance is now: " + Destination.Balance)
  }

}

@fragment
trait Destination {
  this: Account with Context =>

  def deposit(amount: BigDecimal) {
    increaseBalance(amount)
  }
}

trait Context {
  private[moneyTransfer] val Source: Account with Source
  private[moneyTransfer] val Destination: Account with Destination
  // Amount is both a role and stage prop
  val Amount: BigDecimal
}

class ContextImpl(srcAcc: Account, dstAcc: Account, val Amount: BigDecimal) extends Context {

  private[moneyTransfer] val Source = role[Source, Account, Context](srcAcc)
  // The role macro just unfolds in the following code
//  private[moneyTransfer] val Source = {
//    implicit val dataFrag = external[Account](srcAcc)
//    implicit val selfFrag = external[Context](this)
//    singleton[Account with Source with Context].!
//  }

//  private[moneyTransfer] val Destination = {
//    implicit val dataFrag = external[Account](dstAcc)
//    implicit val selfFrag = external[Context](this)
//    singleton[Account with Destination with Context].!
//  }

  private[moneyTransfer] val Destination = role[Destination, Account, Context](dstAcc)


  def trans(): Unit = {
    Source.transfer
  }

}

object UseCase {

  def main(args: Array[String]): Unit = {
    val ctx = new ContextImpl(new AccountImpl(10), new AccountImpl(50), 5)
    ctx.trans()
  }

}