package org.cloudio.morpheus.mail.traditional

import org.cloudio.morpheus.mail.MailOwner

/**
 * Created by zslajchrt on 24/08/15.
 */
trait DefaultUserMail extends UserMail {

  this: MailOwner =>

  override def sendEmail(message: Message): Unit = {
    try {
      validateEmail(message)
      send(message)
    }
    catch {
      case e: IllegalArgumentException =>
        store(message)
    }
  }

  override def validateEmail(message: Message): Unit = {
    // todo
  }

  private def send(message: Message): Unit = {
    // todo
  }

  private def store(message: Message): Unit = {
    // todo
  }

}
