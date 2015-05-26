package org.cloudio.morpheus.tutor.chat.frag.misc.sharedState.case1

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * A demonstration of sharing state between fragments.
 *
 * In this case we use the delegate pattern.
 *
 * Created by zslajchrt on 26/05/15.
 */

trait Contact {
  // mutable members must be abstract
  var name: String
  var address: String
  def sendMessage(msg: String): Unit
}

class DefaultContact(nm: String, ad: String) extends Contact {

  override var name: String = nm

  override var address: String = ad

  override def sendMessage(msg: String): Unit = ???
}

@fragment trait OfflineContact extends Delegate[Contact] {
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


@fragment trait OnlineContact extends Delegate[Contact] {
  override def sendMessage(msg: String) {
    // todo
    println(s"Sending message to $name <$address> by chat")
  }
}

object Session {

  def main(args: Array[String]) {

    type ContactMorphType = OfflineContact or OnlineContact
    val sharedContact = new DefaultContact("Josef", "chat:pepa")
    implicit val offlineFrag = single[OfflineContact, Contact](sharedContact)
    implicit val onlineFrag = single[OnlineContact, Contact](sharedContact)
    val contactKernel = singleton[ContactMorphType]
    val contact = contactKernel.~
    contact.address = "pepa@gmail.com"
    println(contact.address)
    contact.sendMessage("ssss")

    contactKernel.~.remorph(promote[ContactMorphType](1))

    //sharedContact.address = "chat:pepa"
    contact.sendMessage("ssss")

    contact.name = "Josef2"
    contact.sendMessage("ssss")

    contact.address = "chat:pepa2"
    contactKernel.~.remorph(promote[ContactMorphType](0))
    contact.sendMessage("ssss")
  }

}
