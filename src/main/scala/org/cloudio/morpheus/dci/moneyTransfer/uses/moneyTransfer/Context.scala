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

  def transfer(destination: Destination, amount: BigDecimal): Unit = {
    destination.deposit(amount)
    withdraw(amount)
  }

}

@fragment @wrapper
trait BlockedSource extends Source {
  this: Account with BlockedAccount =>

  override def transfer(destination: Destination, amount: BigDecimal): Unit = {
    println("Warning: No transfer. Account blocked.")
    destination.deposit(0)
  }

}

@fragment
trait Destination {
  this: Account =>

  def deposit(amount: BigDecimal): Unit = {
    increaseBalance(amount)
  }
}

class Context(srcAcc: &[$[Source] with Account with /?[$[BlockedSource] with BlockedAccount]],
              dstAcc: &[$[Destination] with Account], val amount: BigDecimal) {

  val source = *(srcAcc).~
  val destination = *(dstAcc).~

  def trans(): Unit = {
    println("Source:" + source.myAlternative)
    println("Destination:" + destination.myAlternative)
    source.transfer(destination, amount)
  }

}

object App {

  def main(args: Array[String]): Unit = {
    val savingsAcc = {
      val model = parse[AccountBase with /?[BlockedAccount] with SavingsAccount](true)
      val strat = unmaskFull[BlockedAccount](model)(rootStrategy(model), {
        case Some(m) if m.balance > 0 => Some(0)
        case _ => None
      })
      singleton(model, strat).~
    }
    savingsAcc.increaseBalance(10)
    savingsAcc.remorph
    println(savingsAcc.myAlternative)

    val checkingAcc = {
      singleton[AccountBase with /?[BlockedAccount] with CheckingAccount].~.remorph
    }

    checkingAcc.increaseBalance(50)

    println(s"Source balance is: ${savingsAcc.balance}")
    println(s"Destination balance is: ${checkingAcc.balance}")

    var ctx = new Context(savingsAcc, checkingAcc, 12)
    ctx.trans()

    println(s"Source balance is now: ${savingsAcc.balance}")
    println(s"Destination balance is now: ${checkingAcc.balance}")

    savingsAcc.remorph
    println(savingsAcc.myAlternative)

    ctx = new Context(savingsAcc, checkingAcc, 12)
    ctx.trans()

    println(s"Source balance is now: ${savingsAcc.balance}")
    println(s"Destination balance is now: ${checkingAcc.balance}")
  }

}