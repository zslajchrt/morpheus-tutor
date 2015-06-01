package org.cloudio.morpheus.tutor.chat.frag.step8

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * Dimension wrappers
 *
 * Created by zslajchrt on 05/05/15.
 */

trait ContactData {
  val firstName: String
  val lastName: String
  val male: Boolean
  val email: String
  val nationality: Locale
}

case class ContactConfig(firstName: String,
                         lastName: String,
                         male: Boolean,
                         email: String,
                         nationality: Locale) extends ContactData

@fragment
trait Contact extends dlg[ContactData] {
  // some calculated fields could be added here, like:
  lazy val female = !male
}


@dimension @wrapper
trait ChannelMonitor extends OutputChannel {

  private var totalChars_ = 0
  def totalChars = totalChars_

  abstract override def printText(text: String): Unit = {
    super.printText(text)
    totalChars_ += text.length
  }
}

@dimension @wrapper
trait NewLineAppender extends OutputChannel {
  abstract override def printText(text: String): Unit = {
    super.printText(text + "\n")
  }
}

object Session {

  def main(args: Array[String]) {

    val contactCfg = ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    implicit val contactFrag = single[Contact, ContactData](contactCfg)
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel) with \?[ChannelMonitor] with \?[NewLineAppender]]

    var printerAltNum: Int = 0
    var channelAltNum: Int = 0
    var channelWrappersAltNum: Int = 3
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelAltNum)
    val morphStrategy3 = promote[\?[ChannelMonitor] with \?[NewLineAppender]](morphStrategy2, channelWrappersAltNum)

    val contact1 = contactCmp.morph(morphStrategy3)
    println(s"Morph composition: ${contact1.myAlternative}")

    contact1.printContact()
    select[ChannelMonitor](contact1) match {
      case None =>
      case Some(chMon) => println(s"Characters written: ${chMon.totalChars}")
    }

  }

}

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

