package org.cloudio.morpheus.tutor.chat.frag.step10

import java.util.Locale

import org.cloudio.morpheus.tutor.chat.frag.step8.{ChannelMonitor, NewLineAppender}
import org.cloudio.morpheus.tutor.chat.frag.step9.{DefaultBufferWatchDogConfig, BufferWatchDogConfig, BufferWatchDog}
import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * Using a composite in another composite
 *
 * Created by zslajchrt on 05/05/15.
 */


//trait ContactData {
//  val firstName: String
//  val lastName: String
//  val email: String
//  val male: Boolean
//  val nationality: Locale
//}
//
//case class ContactConfig(firstName: String,
//                         lastName: String,
//                         male: Boolean,
//                         email: String,
//                         nationality: Locale) extends ContactData
//
//@fragment
//trait Contact extends dlg[ContactData] {
//  // some calculated fields could be added here, like:
//  lazy val female = !male
//}


@dimension
trait EmailService {
  def sendMail(to: String, subject: String, msg: String)
}

@fragment
trait DummyEmailService extends EmailService {
  override def sendMail(to: String, subject: String, msg: String): Unit = {
    println(s"to: $to, subject: $subject, msg: $msg")
  }
}

trait EmailChannelConfig {
  val emailSubject: String
}

case class DefaultEmailChannelConfig(emailSubject: String) extends EmailChannelConfig

@fragment
trait EmailChannel extends OutputChannel with dlg[EmailChannelConfig] {
  this: Contact with EmailService =>

  override def printText(text: String): Unit = {
    sendMail(email, emailSubject, text)
  }
}

@dimension @wrapper
trait EmailVerifier extends EmailService {
  abstract override def sendMail(to: String, subject: String, msg: String): Unit = {
    // todo: verify the address
    super.sendMail(to, subject, msg)
  }
}


trait EmailSignatureAppenderConfig {
  val emailSignature: String
}

case class EmailSignatureAppender_(emailSignature: String) extends EmailSignatureAppenderConfig

@dimension @wrapper
trait EmailSignatureAppender extends EmailService with EmailSignatureAppenderConfig {
  abstract override def sendMail(to: String, subject: String, msg: String): Unit = {
    super.sendMail(to, subject, s"$msg\n$emailSignature")
  }
}

object Session {

  def main(args: Array[String]) {
    val emailServiceKernel = singleton[DummyEmailService]
    val emailService = emailServiceKernel.!
    emailService.sendMail("pepa@gmail.com", "Hello!", "Hello, Pepa!")

    implicit val contactFrg = single[OfflineContact, Contact](ContactData("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](DefaultEmailChannelConfig("Contact"))
    implicit val emailServiceFrg = external[EmailService](emailService)
    val contactCmp = singleton[OfflineContact with ContactPrettyPrinter with EmailService with EmailChannel]
    val contact = contactCmp.!

    contact.printContact()

  }

  def main2(args: Array[String]) {
    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier]
    val emailService = emailServiceCmp.!

    val contactData = ContactData("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)
    implicit val bufWDFrag = single[BufferWatchDog, BufferWatchDogConfig](DefaultBufferWatchDogConfig(10))
    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](DefaultEmailChannelConfig("Contact"))
    implicit val emailServiceFrg = external[EmailService](emailService)

    // 64 alternatives
    val contactKernel = singleton[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or (MemoryOutputChannel with \?[BufferWatchDog]) or (EmailChannel with EmailService)) with
      \?[NewLineAppender] with
      \?[ChannelMonitor]]

    var contact = contactKernel.!
    val emailChannel = asMorphOf[EmailChannel](contact)
    emailChannel.printText("message")

    var contactCoord: Int = 1
    val contactStr = promote[OfflineContact or OnlineContact](
      RootStrategy[contactKernel.Model](), contactCoord)

    var printerCoord: Int = 1
    val printerStr = promote[ContactRawPrinter or ContactPrettyPrinter](
      contactStr, printerCoord)

    var channelCoord: Int = 2
    val channelStr = promote[StandardOutputChannel or MemoryOutputChannel or EmailChannel](
      printerStr, channelCoord)

    var bufferCoord: Int = 1

    val bufferWatchDogStr = promote[\?[BufferWatchDog]](channelStr, dependOnAlt(channelStr, 1, bufferCoord))

    var channelWrappersCoord: Int = 3
    val wrappersStr = promote[\?[ChannelMonitor] with \?[NewLineAppender]](
      bufferWatchDogStr, channelWrappersCoord)

    contact = contact.remorph(wrappersStr)
    println(s"Morph composition: ${contact.myAlternative}")

    //contact.printContact()

  }

//  /**
//   * Orchestrating multiple interconnected composites
//   */
//  def main3(args: Array[String]) {
//    implicit val signAppFrg = single[EmailSignatureAppender, EmailSignatureAppenderConfig](EmailSignatureAppender_(
//      """
//         Best regards
//         Morpheus
//      """))
//
//    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier with \?[EmailSignatureAppender]]
//    var signAppAltNum = 1
//    val emailStrategy = promote[\?[EmailSignatureAppender]](RootStrategy[emailServiceCmp.Model](), signAppAltNum)
//    val emailService = emailServiceCmp.morph_~(emailStrategy)
//
//    implicit val contactFrg = single[Contact, ContactData](ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
//    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](EmailChannelConfig_("Contact"))
//    implicit val emailServiceFrg = external[EmailService](emailService)
//    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with EmailService with EmailChannel]
//    var printerAltNum = 0
//    val contactStrategy = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
//    val contact = contactCmp.morph_~(contactStrategy)
//
//    contact.printContact()
//
//    signAppAltNum = 0
//    printerAltNum = 1
//    emailService.remorph()
//    contact.remorph()
//
//    contact.printContact()
//  }
//
//  /**
//   * Orchestrating multiple interconnected composites via event monitors
//   */
//  def main4(args: Array[String]) {
//    val remorphEmailServiceEv = CompositeEvent("remorphEmailService", null, null)
//    val emailSignAppMonitor = EventMonitor[Int]("emailSignAppAltNum", remorphEmailServiceEv)
//
//    val remorphContactEv = CompositeEvent("remorphContact", null, null)
//    val contactPrinterMonitor = EventMonitor[Int]("contactPrinterAltNum", remorphContactEv)
//
//    implicit val mutFrg = mutableFragment(emailSignAppMonitor, contactPrinterMonitor)
//
//    implicit val signAppFrg = single[EmailSignatureAppender, EmailSignatureAppenderConfig](EmailSignatureAppender_(
//      """
//       Best regards
//       Morpheus
//    """))
//    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier with \?[EmailSignatureAppender] with MutableFragment]
//    val emailStrategy = promote[\?[EmailSignatureAppender]](RootStrategy[emailServiceCmp.Model](), emailSignAppMonitor())
//    val emailService = emailServiceCmp.morph_~(emailStrategy)
//    emailService.startListening(remorphEmailServiceEv.nameSelector)
//
//    implicit val contactFrg = single[Contact, ContactData](ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
//    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](EmailChannelConfig_("Contact"))
//    implicit val emailServiceFrg = external[EmailService](emailService)
//    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with EmailService with EmailChannel with MutableFragment]
//    val contactStrategy = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), contactPrinterMonitor())
//    val contact = contactCmp.morph_~(contactStrategy)
//    contact.startListening(remorphContactEv.nameSelector)
//
//    emailService.fireEvent(emailSignAppMonitor.makeEvent(1))
//    contact.fireEvent(contactPrinterMonitor.makeEvent(0))
//
//    contact.printContact()
//
//    emailService.fireEvent(emailSignAppMonitor.makeEvent(0))
//    contact.fireEvent(contactPrinterMonitor.makeEvent(1))
//
//    contact.printContact()
//
//    emailService.stopListening()
//    contact.stopListening()
//  }

}

//@dimension
//trait OutputChannel {
//  def printText(text: String): Unit
//}
//
//@fragment
//trait StandardOutputChannel extends OutputChannel {
//  override def printText(text: String): Unit = print(text)
//}
//
//@fragment
//trait MemoryOutputChannel extends OutputChannel {
//
//  val outputBuffer = new StringBuilder()
//
//  override def printText(text: String): Unit = outputBuffer.append(text)
//
//}
//
//@dimension
//trait ContactPrinter {
//  def printContact(): Unit
//}
//
//@fragment
//trait ContactRawPrinter extends ContactPrinter {
//  this: Contact with OutputChannel =>
//
//  def printContact(): Unit = {
//    printText(s"$firstName $lastName $nationality $male")
//  }
//}
//
//@fragment
//trait ContactPrettyPrinter extends ContactPrinter {
//  this: Contact with OutputChannel =>
//
//  def printContact(): Unit = {
//    printText(
//      s"""
//         First Name: $firstName
//         Second Name: $lastName
//         Male: $male
//         Nationality: $nationality
//      """)
//  }
//}
//
