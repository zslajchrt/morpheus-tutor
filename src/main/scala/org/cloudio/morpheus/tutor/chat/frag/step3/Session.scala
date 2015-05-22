package org.cloudio.morpheus.tutor.chat.frag.step3

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact

/**
 * Abstract the printing capability.
 *
 * The illustration of the problem of reusing the same object with
 *
 * Created by zslajchrt on 04/05/15.
 */

@dimension
trait ContactPrinter {
  def printContact(): Unit
}

@fragment
trait ContactRawPrinter extends ContactPrinter {
  this: Contact =>

  def printContact(): Unit = {
    println(s"$firstName $lastName $nationality $male")
  }
}

@fragment
trait ContactPrettyPrinter extends ContactPrinter {
  this: Contact =>

  def printContact(): Unit = {
    println(
      s"""
         First Name: $firstName
         Second Name: $lastName
         Male: $male
         Nationality: $nationality
      """)
  }
}

object Session {

  def main(args: Array[String]) {

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactScala = if (prettyPrint) {
      new Contact with ContactPrettyPrinter
    } else {
      new Contact with ContactRawPrinter
    }

    val contact = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter].!
    else
      singleton[Contact with ContactRawPrinter].!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    // Passing the contact to a Java method is possible. The method requires a 'Contact with ContactPrinter' instance as the argument.
    // The 'Pimp My Library' pattern would not work since it is a simple wrapper spiced by an implicit conversion.
    ContactClient.useContact(contact)

    // The reuse problem:
    // If we want to print the contact by the other printer we have to create a new instance of the contact.
    // There is no simple way to reuse the existing entity and couple it with another traits.
    // Note: The "pimp my library" pattern could help, however, it is limited to Scala and it is just a syntax trick.

  }

  def main2(args: Array[String]) {

    // The reuse problem solution via re-composition

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactRef: &[Contact with ContactPrinter] = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter]
    else
      singleton[Contact with ContactRawPrinter]

    val contactCmp = *(contactRef)
    val contact = contactCmp.!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    implicit val clientFrag = expose[Contact](contactCmp)
    val contact2 = if (!prettyPrint)
        singleton[Contact with ContactPrettyPrinter].!
      else
        singleton[Contact with ContactRawPrinter].!

    contact2.printContact()
  }

  def main3(args: Array[String]) {

    // The reuse problem solution via composite references:

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactRef: &[Contact with ContactPrinter] = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter]
    else
      singleton[Contact with ContactRawPrinter]

    val contactCmp = *(contactRef)
    val contact = contactCmp.!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    val contact2Ref: &[Contact with $[ContactPrinter]] = contactCmp
    val printFrag = if (prettyPrint)
      singleAsDim[ContactRawPrinter]
    else
      singleAsDim[ContactPrettyPrinter]
    val contact2Cmp = *(contact2Ref, printFrag)

    contact2Cmp.!.printContact()

  }

  /**
   * Using the Pimp My Library pattern for a comparison.
   */
  def main4(args: Array[String]) {

    object StdPr {
      implicit class StandardPrinterCaster(c: Contact) extends ContactPrinter {
        override def printContact(): Unit = {
          import c._
          print(s"$firstName $lastName $nationality $male")
        }
      }
    }

    object MemPr {
      implicit class StandardPrinterCaster(c: Contact) extends ContactPrinter {
        override def printContact(): Unit = {
          import c._
          print(s"$firstName $lastName $nationality $male")
        }
      }
    }

    val prettyPrint = args.contains("prettyPrint")

    val contact = new Contact {}

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    if (prettyPrint) {
      import StdPr._
      contact.printContact()
    } else {
      import MemPr._
      contact.printContact()
    }

    //ContactClient.useContact(contact) // Does not compile
  }


}
