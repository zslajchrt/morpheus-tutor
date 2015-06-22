package org.cloudio.morpheus.dci.moneyTransfer.uses.moneyTransfer

import org.cloudio.morpheus.dci.moneyTransfer.data._

import org.morpheus.Morpheus._
import org.morpheus._
import org.morpheus.dci.DCI._

/**
* Created by zslajchrt on 22/06/15.
*/

@dimension @wrapper
trait Source extends Account {
  this: Context =>

  // Designing the role as a dimension wrapper prevents us from having method name clashes.
  // For example, the compiler will complain unless the following method explicitly mentions `abstract override`, which
  // informs that it is designed as a wrapper of the method from Account.
  //
//  def decreaseBalance(amount: BigDecimal): Unit = {
//      println(s"Decreasing of $amount")
//  }

  def withdraw(amount:BigDecimal) {
    source.decreaseBalance(amount)
  }

  def transfer(amount:BigDecimal) {
    Console.println("Source balance is: " + source.Balance)
    Console.println("Destination balance is: " + destination.Balance)

    destination.deposit(amount)
    source.withdraw(amount)

    Console.println("Source balance is now: " + source.Balance)
    Console.println("Destination balance is now: " + destination.Balance)
  }

}

@dimension @wrapper
trait Destination extends Account {
  this: Context =>

  def deposit(amount:BigDecimal) {
    destination.increaseBalance(amount)
  }
}

trait Context {
  val source: Source
  val destination: Destination
}

class ContextImpl(srcAcc: Account, dstAcc: Account, amount: BigDecimal) extends Context {

  val source = role[Source, Account, Context](srcAcc)

  val destination = role[Destination, Account, Context](dstAcc)

  def trans(): Unit = {
    source.transfer(amount)
  }

}

object UseCase {

  def main(args: Array[String]): Unit = {
    val ctx = new ContextImpl(new AccountImpl(10), new AccountImpl(50), 5)
    ctx.trans()
  }

}