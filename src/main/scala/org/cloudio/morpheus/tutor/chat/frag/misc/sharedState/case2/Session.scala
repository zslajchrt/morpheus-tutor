package org.cloudio.morpheus.tutor.chat.frag.misc.sharedState.case2

import org.morpheus._
import org.morpheus.Morpheus._

/**
* A demonstration of sharing state between fragments.
*
* In this case we separate the common state to a new fragment.
*
* Created by zslajchrt on 26/05/15.
*/

@fragment trait Contact {
  var name: String = null
}

trait ContactState {
  var address: String
  def sendMessage(msg: String): Unit
}

@fragment trait OfflineContact extends ContactState {
  this: Contact =>

  private var email: String = null
  override def address: String = email
  override def address_=(em: String): Unit = {
    email = em
  }

  override def sendMessage(msg: String) {
    // todo
    println(s"Sending message to $name <$address> by email")
  }
}


@fragment trait OnlineContact extends ContactState {
  this: Contact =>

  private var chatId: String = null
  override def address: String = chatId
  override def address_=(em: String): Unit = {
    chatId = em
  }

  override def sendMessage(msg: String) {
    // todo
    println(s"Sending message to $name <$address> by chat")
  }
}


object Session {
  def main(args: Array[String]) {
    type ContactMorphType = Contact with (OfflineContact or OnlineContact)
    val contactKernel = singleton[ContactMorphType]
    val contact = contactKernel.~
    contact.name = "Josef"
    contact.address = "pepa@gmail.com"
    println(contact.address)
    contact.sendMessage("ssss")

    contactKernel.~.remorph(promote[ContactMorphType](1))

    contact.address = "chat:pepa"
    contact.sendMessage("ssss")

    contact.name = "Josef2"
    contact.sendMessage("ssss")

    contact.address = "chat:pepa2"
    contactKernel.~.remorph(promote[ContactMorphType](0))
    contact.sendMessage("ssss")

  }
}
