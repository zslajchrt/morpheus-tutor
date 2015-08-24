package org.cloudio.morpheus.mail.traditional

import java.util
import java.util.Date

import org.cloudio.morpheus.mail.{Attachment, UserMail}

/**
 * Created by zslajchrt on 24/08/15.
 */
trait RegisteredUserMail extends UserMail {
  this: RegisteredUser =>

  abstract override def sendEmail(recipients: util.List[String], subject: String, message: String, attachments: util.List[Attachment]): Unit = {
    val now: Date = new Date()
    if (validTo.compareTo(now) < 0) {
      throw new IllegalStateException("Expired account")
    }
    super.sendEmail(recipients, subject, message, attachments)
  }
}
