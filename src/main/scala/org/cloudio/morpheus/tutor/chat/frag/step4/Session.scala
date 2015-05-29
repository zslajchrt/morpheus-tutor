package org.cloudio.morpheus.tutor.chat.frag.step4

import java.util.Locale

import org.cloudio.morpheus.tutor.chat.frag.step3.ContactSerializer
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

    val (style, channel) = (System.getProperty("style", "raw"), System.getProperty("channel", "standard"))

    val contact = (style, channel) match {
      case ("pretty", "memory") => singleton[Contact with ContactPrettyPrinter with MemoryOutputChannel].!
      case ("raw", "memory") => singleton[Contact with ContactRawPrinter with MemoryOutputChannel].!
      case ("pretty", "standard") => singleton[Contact with ContactPrettyPrinter with StandardOutputChannel].!
      case ("raw", "standard") => singleton[Contact with ContactRawPrinter with StandardOutputChannel].!
    }

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

  }

  def main(args: Array[String]) {

    val contactKernel = singleton[Contact
      with (ContactRawPrinter or ContactPrettyPrinter)
      with (StandardOutputChannel or MemoryOutputChannel)]

    val contact = contactKernel.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()
    println(contact.myAlternative)

    //asMorphOf[ContactRawPrinter with ContactSerializer](contact) // it won't compile
    val standardRawPrinter = asMorphOf[ContactRawPrinter with StandardOutputChannel](contact)
    standardRawPrinter.printContact()
    println(standardRawPrinter.myAlternative)

    val memoryPrettyPrinter = asMorphOf[ContactPrettyPrinter with MemoryOutputChannel](contact)
    memoryPrettyPrinter.printContact()
    println(memoryPrettyPrinter.myAlternative)

    //select[ContactSerializer](contact) // it won't compile
    //select[ContactRawPrinter](memoryPrettyPrinter) // it won't compile
    select[Contact with ContactPrettyPrinter](memoryPrettyPrinter) // it should compile despite Contact is not explicitly mentioned in the morph type, however, it is a dependency of ContactPrettyPrinter

    select[MemoryOutputChannel](memoryPrettyPrinter) match {
      case None =>
      case Some(memChannel) =>
        println(memChannel.outputBuffer)
    }

  }
}
