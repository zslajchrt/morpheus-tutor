package org.cloudio.morpheus.tutor.chat.frag.step10

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * Using a composite in another composite
 *
 * Created by zslajchrt on 05/05/15.
 */

@dimension
trait EmailService {
  def sendMail(to: String, subject: String, msg: String)
}

@dimension @wrapper
trait EmailVerifier extends EmailService {
  abstract override def sendMail(to: String, subject: String, msg: String): Unit = {
    // todo: verify the address
    super.sendMail(to, subject, msg)
  }
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

case class EmailChannelConfig_(emailSubject: String) extends EmailChannelConfig

@fragment
trait EmailChannel extends OutputChannel with EmailChannelConfig {
  this: Contact with EmailService =>

  override def printText(text: String): Unit = {
    sendMail(email, emailSubject, text)
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

  def main1(args: Array[String]) {
    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier]
    val emailService = emailServiceCmp.!

    implicit val contactFrg = single[Contact, ContactConfig](ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](EmailChannelConfig_("Contact"))
    implicit val emailServiceFrg = external[EmailService](emailService)
    val contactCmp = singleton[Contact with ContactPrettyPrinter with EmailService with EmailChannel]
    val contact = contactCmp.!

    contact.printContact()

  }

  /**
   * Orchestrating multiple interconnected composites
   */
  def main2(args: Array[String]) {
    implicit val signAppFrg = single[EmailSignatureAppender, EmailSignatureAppenderConfig](EmailSignatureAppender_(
      """
         Best regards
         Morpheus
      """))

    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier with \?[EmailSignatureAppender]]
    var signAppAltNum = 1
    val emailStrategy = promote[\?[EmailSignatureAppender]](RootStrategy[emailServiceCmp.Model](), signAppAltNum)
    val emailService = emailServiceCmp.morph_~(emailStrategy)

    implicit val contactFrg = single[Contact, ContactConfig](ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](EmailChannelConfig_("Contact"))
    implicit val emailServiceFrg = external[EmailService](emailService)
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with EmailService with EmailChannel]
    var printerAltNum = 0
    val contactStrategy = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val contact = contactCmp.morph_~(contactStrategy)

    contact.printContact()

    signAppAltNum = 0
    printerAltNum = 1
    emailService.remorph()
    contact.remorph()

    contact.printContact()
  }

  /**
   * Orchestrating multiple interconnected composites via event monitors
   */
  def main(args: Array[String]) {
    val remorphEmailServiceEv = CompositeEvent("remorphEmailService", null, null)
    val emailSignAppMonitor = EventMonitor[Int]("emailSignAppAltNum", remorphEmailServiceEv)

    val remorphContactEv = CompositeEvent("remorphContact", null, null)
    val contactPrinterMonitor = EventMonitor[Int]("contactPrinterAltNum", remorphContactEv)

    implicit val mutFrg = mutableFragment(emailSignAppMonitor, contactPrinterMonitor)

    implicit val signAppFrg = single[EmailSignatureAppender, EmailSignatureAppenderConfig](EmailSignatureAppender_(
      """
       Best regards
       Morpheus
    """))
    val emailServiceCmp = singleton[DummyEmailService with EmailVerifier with \?[EmailSignatureAppender] with MutableFragment]
    val emailStrategy = promote[\?[EmailSignatureAppender]](RootStrategy[emailServiceCmp.Model](), emailSignAppMonitor())
    val emailService = emailServiceCmp.morph_~(emailStrategy)
    emailService.startListening(remorphEmailServiceEv.nameSelector)

    implicit val contactFrg = single[Contact, ContactConfig](ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    implicit val emailChannelFrg = single[EmailChannel, EmailChannelConfig](EmailChannelConfig_("Contact"))
    implicit val emailServiceFrg = external[EmailService](emailService)
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with EmailService with EmailChannel with MutableFragment]
    val contactStrategy = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), contactPrinterMonitor())
    val contact = contactCmp.morph_~(contactStrategy)
    contact.startListening(remorphContactEv.nameSelector)

    emailService.fireEvent(emailSignAppMonitor.makeEvent(1))
    contact.fireEvent(contactPrinterMonitor.makeEvent(0))

    contact.printContact()

    emailService.fireEvent(emailSignAppMonitor.makeEvent(0))
    contact.fireEvent(contactPrinterMonitor.makeEvent(1))

    contact.printContact()

    emailService.stopListening()
    contact.stopListening()
  }

}
