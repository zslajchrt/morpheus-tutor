package org.cloudio.morpheus.tutor.chat.frag.step0

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 25/05/15.
 */

trait Contact {
  def address: String
  def sendMessage(msg: String)
}

@fragment trait OfflineContact extends Contact {
  var emailAddress: String = _
  def address = emailAddress
  override def sendMessage(msg: String) {
    // todo
    println(s"Sending message to $emailAddress by email")
  }
}

@fragment trait OnlineContact extends Contact {
  var chatNick: String = _
  def address = chatNick
  override def sendMessage(msg: String) {
    // todo
    println(s"Sending message to $chatNick by chat")
  }
}

object Session {

  def main(args: Array[String]) {

    type ContactMorphType = OfflineContact or OnlineContact
    val contactKernel = singleton[ContactMorphType]
    val contact: Contact = contactKernel.~

    select[OfflineContact](contactKernel.~) match {
    case None => sys.error("unexpected alternative")
    case Some(offlineContact) => offlineContact.emailAddress = args(0)
    }

    // select[String](contactKernel.~) // it does not compile since String is not a valid alternative

    var contactAlt = 1
    val contactMorphStrategy = promote[ContactMorphType](contactAlt)
    contactKernel.~.remorph(contactMorphStrategy)

    select[OnlineContact](contactKernel.~) match {
    case None => sys.error("unexpected alternative")
    case Some(onlineContact) => onlineContact.chatNick = args(1)
    }

    contactAlt = 0 // emulate “went-offline” event
    contactKernel.~.remorph
    println(s"Recipient address ${contactKernel.~.address}")
    contactKernel.~.sendMessage("Hello!")

    contactAlt = 1 // emulate “went-online” event
    contactKernel.~.remorph
    println(s"Recipient address ${contactKernel.~.address}")
    contactKernel.~.sendMessage("Hello again!")

  }

}

