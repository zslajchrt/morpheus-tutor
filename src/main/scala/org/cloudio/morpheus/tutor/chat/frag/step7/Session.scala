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

@dimension
trait Contact {
  val firstName: String
  val lastName: String
  val male: Boolean
  val nationality: Locale
}

case class ContactData(firstName: String,
                        lastName: String,
                        male: Boolean,
                        nationality: Locale) extends Contact

@fragment
trait OfflineContact extends dlg[Contact] {
}

@fragment
trait OnlineContact extends dlg[Contact] {
}

object App {

  def main(args: Array[String]) {

    val contactData = ContactData("Pepa", "Novák", male = true, Locale.CANADA)

    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)
    val contactKernel = singleton[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactKernel.~

    var contactCoord: Int = 0
    val contactDimStr = promote[OfflineContact or OnlineContact](contactKernel.defaultStrategy, contactCoord)
    var printerCoord: Int = 0
    val printerDimStr = promote[ContactRawPrinter or ContactPrettyPrinter](contactDimStr, printerCoord)
    var channelCoord: Int = 0
    val channelDimStr = promote[StandardOutputChannel or MemoryOutputChannel](printerDimStr, channelCoord)
    contact.remorph(channelDimStr)

    contact.printContact()

    contactCoord = 1
    printerCoord = 1
    channelCoord = 0

    contact.remorph()
    contact.printContact()

//    // Note: the only mutable part is the buffer in the memory channel
//
//    altNum = 0
//    val contact3 = contactKernel.morph_~(morphStrategy)
//    contact3.printContact()
//    altNum = 1
//    contact3.remorph()
//    contact3.printContact()

    // Note: there is only one mutable part besides the memory channel's buffer, which is the atomic reference in
    // the contact3 morph proxy holding the immutable morph.
  }

  //
  //  /**
  //   * Using a composite reference with a parameter to clone a prefabricated prototype composite.
  //   */
  //  def main2(args: Array[String]) {
  //
  //    type ContactAddOns = (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)
  //
  //    val contactKernel = singleton_?[ContactAddOns]
  //    val contactRef: &?[$[Contact] with ContactAddOns] = contactKernel
  //
  //    val contactCfg = ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
  //    val contactKernel1 = *(contactRef, single[Contact, ContactData](contactCfg))
  //
  //    val chinaConfig = contactCfg.copy(nationality = Locale.CHINA)
  //    val contactFrag2 = single[Contact, ContactData](chinaConfig)
  //    val contactKernel2 = *(contactRef, contactFrag2)
  //    contactKernel1.!.printContact()
  //    contactKernel2.!.printContact()
  //  }
  //
  //}
  //
  //

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
  this: (OfflineContact or OnlineContact) with OutputChannel =>

  def printContact(): Unit = {
    select[Contact](this) match {
      case None =>
      case Some(contact) =>
        printText(
          s"""
         First Name: ${contact.firstName}
         Second Name: ${contact.lastName}
         Male: ${contact.male}
         Nationality: ${contact.nationality}
      """)

        contact match {
          case offline: OfflineContact =>
            println("is offline")
          case online: OnlineContact =>
            println("is online")
        }
    }

  }
}

