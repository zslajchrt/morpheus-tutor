package org.cloudio.morpheus.dci.moneyTransfer.uses.moneyTransfer

import org.cloudio.morpheus.dci.moneyTransfer.data._

import org.morpheus.Morpheus._
import org.morpheus._
import org.morpheus.dci.DCI._

/**
 * Created by zslajchrt on 22/06/15.
 */

@dimension
@wrapper
trait Source extends Account {
  this: Context =>

  // Designing the role as a dimension wrapper prevents us from having method name clashes.
  // For example, the compiler will complain unless the following method explicitly mentions `abstract override`, which
  // informs that it is designed as a wrapper of the method from Account.
  //
  //  def decreaseBalance(amount: BigDecimal): Unit = {
  //      println(s"Decreasing of $amount")
  //  }

  private def withdraw(amount: BigDecimal) {
    Source.decreaseBalance(amount)
  }

  def transfer {
    Console.println("Source balance is: " + Source.Balance)
    Console.println("Destination balance is: " + Destination.Balance)

    Destination.deposit(Amount)
    Source.withdraw(Amount)

    Console.println("Source balance is now: " + Source.Balance)
    Console.println("Destination balance is now: " + Destination.Balance)
  }

}

@dimension
@wrapper
trait Destination extends Account {
  this: Context =>

  def deposit(amount: BigDecimal) {
    Destination.increaseBalance(amount)
  }
}

trait Context {
  private[moneyTransfer] val Source: Source
  private[moneyTransfer] val Destination: Destination
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