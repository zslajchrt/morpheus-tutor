package org.cloudio.morpheus.tutor.chat.frag.step9

import java.util.Locale

import org.morpheus._
import Morpheus._

/**
 * Fragment wrappers
 *
 * Created by zslajchrt on 05/05/15.
 */

trait ContactData {
  val firstName: String
  val lastName: String
  val email: String
  val male: Boolean
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


trait BufferWatchDogConfig {
  val bufferWatchDogLimit: Int
}

case class BufferWatchDogConfig_(bufferWatchDogLimit: Int) extends BufferWatchDogConfig

@fragment @wrapper
trait BufferWatchDog extends MemoryOutputChannel with BufferWatchDogConfig {

  override def printText(text: String): Unit = {
    super.printText(text)
    if (outputBuffer.size > bufferWatchDogLimit) {
      print(outputBuffer)
      outputBuffer.clear()
    }
  }

}

object Session {

  def main(args: Array[String]) {

    val contactCfg = ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    implicit val contactFrag = single[Contact, ContactData](contactCfg)
    implicit val bufWDFrag = single[BufferWatchDog, BufferWatchDogConfig](BufferWatchDogConfig_(100))
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or (MemoryOutputChannel with \?[BufferWatchDog]))]

    var printerAltNum: Int = 0
    var channelAltNum: Int = 1
    var bufferWDAltNum: Int = 1
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelAltNum)
    val morphStrategy3 = promote[\?[BufferWatchDog]](morphStrategy2, bufferWDAltNum)

    val contact1 = contactCmp.morph(morphStrategy3)
    println(s"Morph composition: ${contact1.myAlternative}")

    for (i <- 0 to 50) {
      contact1.printContact()
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

