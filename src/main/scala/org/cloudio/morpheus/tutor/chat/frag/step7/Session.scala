package org.cloudio.morpheus.tutor.chat.frag.step7

import java.util.Locale

import org.morpheus._
import Morpheus._

/**
 * Making the Contact entity immutable.
 *
 * Created by zslajchrt on 04/05/15.
 */
object Session {
}

trait ContactConfig {
  val firstName: String
  val lastName: String
  val male: Boolean
  val email: String
  val nationality: Locale
}

case class ContactConfig_(firstName: String,
                          lastName: String,
                          male: Boolean,
                          email: String,
                          nationality: Locale) extends ContactConfig

@fragment
trait Contact extends ContactConfig {
  // some calculated fields could be added here, like:
  lazy val female = !male
}


object App {

  def main(args: Array[String]) {

    val contactCfg = ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    implicit val contactFrag = single[Contact, ContactConfig](contactCfg)
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]

    var altNum: Int = 0
    val morphStrategy = promote[contactCmp.Model](altNum)

    val contact1 = contactCmp.morph(morphStrategy)
    contact1.printContact()
    val isFemale = contact1.female // try the calculated field
    altNum = 1
    val contact2 = contactCmp.morph(morphStrategy)
    contact2.printContact()

    // Note: the only mutable part is the buffer in the memory channel

    altNum = 0
    val contact3 = contactCmp.morph_~(morphStrategy)
    contact3.printContact()
    altNum = 1
    contact3.remorph()
    contact3.printContact()

    // Note: there is only one mutable part besides the memory channel's buffer, which is the atomic reference in
    // the contact3 morph proxy holding the immutable morph.
  }

  /**
   * Using a composite reference with a parameter to clone a prefabricated prototype composite.
   */
  def main2(args: Array[String]) {

    type ContactAddOns = (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)

    val contactCmp = singleton_?[ContactAddOns]
    val contactRef: &?[$[Contact] with ContactAddOns] = contactCmp

    val contactCfg = ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    val contactCmp1 = *(contactRef, single[Contact, ContactConfig](contactCfg))

    val contactFrag2 = single[Contact, ContactConfig](contactCfg.copy(nationality = Locale.CHINA))
    val contactCmp2 = *(contactRef, contactFrag2)
    contactCmp1.!.printContact()
    contactCmp2.!.printContact()
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

