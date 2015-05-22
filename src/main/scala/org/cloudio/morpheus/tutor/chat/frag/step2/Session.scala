package org.cloudio.morpheus.tutor.chat.frag.step2

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact

/**
 * Adding some behavior to the entity by means of the cake pattern.
 *
 * Created by zslajchrt on 04/05/15.
 */

@fragment
trait ContactPrinter {
  this: Contact =>

  def printContact(): Unit = {
    println(s"$firstName $lastName $nationality $male")
  }
}

object Session {

  def main(args: Array[String]) {

    val contactScala = new Contact with ContactPrinter
    contactScala.printContact()

    // todo: use 'stock' instead of 'pool'
    val contact = singleton[Contact with ContactPrinter].!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

  }

}
