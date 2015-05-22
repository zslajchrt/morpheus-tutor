package org.cloudio.morpheus.tutor.chat.frag.step4

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact

import scala.StringBuilder
import scala.collection.mutable

/**
 * Let's add another dimension.
 *
 * The illustration of the combinatorial explosion and its solution.
 *
 * Created by zslajchrt on 04/05/15.
 */


@dimension
trait OutputChannel {
  def printText(text: String): Unit
}

@fragment
trait StandardOutputChannel extends OutputChannel {
  override def printText(text: String): Unit = print(text)
}

@fragment
trait MemoryOutputChannel extends OutputChannel {

  val outputBuffer = new StringBuilder()

  override def printText(text: String): Unit = outputBuffer.append(text)

}

@dimension
trait ContactPrinter {
  def printContact(): Unit
}

@fragment
trait ContactRawPrinter extends ContactPrinter {
  this: Contact with OutputChannel =>

  def printContact(): Unit = {
    printText(s"$firstName $lastName $nationality $male")
  }
}

@fragment
trait ContactPrettyPrinter extends ContactPrinter {
  this: Contact with OutputChannel =>

  def printContact(): Unit = {
    printText(
      s"""
         First Name: $firstName
         Second Name: $lastName
         Male: $male
         Nationality: $nationality
      """)
  }
}



object Session {

  def main1(args: Array[String]) {

    val (prettyPrint, memoryOutput) = (args.contains("prettyPrint"), args.contains("memoryOutput"))

    val contact = (prettyPrint, memoryOutput) match {
      case (true, true) => singleton[Contact with ContactPrettyPrinter with MemoryOutputChannel].!
      case (false, true) => singleton[Contact with ContactRawPrinter with MemoryOutputChannel].!
      case (true, false) => singleton[Contact with ContactPrettyPrinter with StandardOutputChannel].!
      case (false, false) => singleton[Contact with ContactRawPrinter with StandardOutputChannel].!
    }

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

  }

  def main(args: Array[String]) {

    val contactCmp = singleton[Contact
      with (ContactRawPrinter or ContactPrettyPrinter)
      with (StandardOutputChannel or MemoryOutputChannel)]

    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()
    println(contact.myAlternative)

    val printer1 = asCompositeOf[ContactRawPrinter with StandardOutputChannel](contactCmp)
    printer1.printContact()

    val printer2 = asCompositeOf[ContactPrettyPrinter with MemoryOutputChannel](contactCmp)
    printer2.printContact()

    select[MemoryOutputChannel](printer2) match {
      case None =>
      case Some(memChannel) =>
        println(memChannel.outputBuffer)
    }

  }
}
