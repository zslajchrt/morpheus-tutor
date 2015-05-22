package org.cloudio.morpheus.tutor.chat.frag.step11

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * References
 *
 * Created by zslajchrt on 06/05/15.
 */


@dimension
trait ContactStatus {

}

@fragment
trait Online extends ContactStatus {

}

@fragment
trait Offline extends ContactStatus {

}

trait ContactStatusVisitor[T] {
  def visitOfflineContact(contact: Contact with Offline): T

  def visitOnlineContact(contact: Contact with Online): T
}

class ContactStatusAcceptor(contactStatusRef: &[Contact with (Offline or Online)]) {

  private val contactStatus = *(contactStatusRef).~

  def acceptVisitor[T](vis: ContactStatusVisitor[T]): T = {
    // refresh the status, it does not re-instantiate the proxy's delegate unless its composition changes
    contactStatus.notifyMorpher()

    contactStatus.delegate match {
      case c: Contact with Offline => vis.visitOfflineContact(c)
      case c: Contact with Online => vis.visitOnlineContact(c)
      case _ => sys.error("Unexpected status")
    }
  }
}

class ContactStatusController(contactStatusRef: &[Offline or Online]) {

  def setStatus(active: Boolean): Unit = {
    remorph(contactStatusRef, if (active) 1 else 0)
  }

}


object Session {

  def main(args: Array[String]) {

    implicit val contactFrg = single[Contact, ContactConfig](ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    val contactModel = parse[Contact with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel) with
      (Offline or Online)](true)

    var printerAltNum: Int = 0
    var channelAltNum: Int = 0
    var statusAltNum: Int = 0


    val strategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactModel.Model](), printerAltNum)
    val strategy2 = promote[StandardOutputChannel or MemoryOutputChannel](strategy1, channelAltNum)
    val strategy3 = promote[Offline or Online](strategy2, statusAltNum)
    val contactCmp = singleton(contactModel, strategy3)

    val contactAcceptor = new ContactStatusAcceptor(contactCmp.~) // using contactCmp.~ instead contactCmp links the reference with the source morph via its current alternatives
    val contactVisitor = new ContactStatusVisitor[Unit] {
      override def visitOfflineContact(contact: Contact with Offline): Unit = {
        println(s"${contact.lastName} is offline")
      }

      override def visitOnlineContact(contact: Contact with Online): Unit = {
        println(s"${contact.lastName} is online")
      }
    }
    contactAcceptor.acceptVisitor(contactVisitor)

    statusAltNum = 1
    contactCmp.~.notifyMorpher()

    contactAcceptor.acceptVisitor(contactVisitor)

    statusAltNum = 0
    contactCmp.~.notifyMorpher()

    contactAcceptor.acceptVisitor(contactVisitor)

    statusAltNum = 1
    contactCmp.~.notifyMorpher()

    contactAcceptor.acceptVisitor(contactVisitor)

    // Controller

    val cc = contactCmp.~
    cc.notifyMorpher()

    val controller: ContactStatusController = new ContactStatusController(contactCmp)
    controller.setStatus(false)

    contactAcceptor.acceptVisitor(contactVisitor)

    controller.setStatus(true)

    contactAcceptor.acceptVisitor(contactVisitor)
  }

}
