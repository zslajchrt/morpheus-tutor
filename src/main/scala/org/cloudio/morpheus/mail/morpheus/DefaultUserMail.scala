package org.cloudio.morpheus.mail.morpheus

import org.morpheus.fragment

/**
 * Created by zslajchrt on 24/08/15.
 */
@fragment
trait DefaultUserMail extends UserMail {

  this: MailOwner =>

  override def sendEmail(message: Message): Unit = {
    try {
      validateEmail(message)
      send(message)
    }
    catch {
      case t: Throwable =>
        store(message)
        throw t
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
