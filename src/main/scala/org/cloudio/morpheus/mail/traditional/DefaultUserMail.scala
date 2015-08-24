package org.cloudio.morpheus.mail.traditional

import java.util

import org.cloudio.morpheus.mail.{MailOwner, Attachment, UserMail}

/**
 * Created by zslajchrt on 24/08/15.
 */
trait DefaultUserMail extends UserMail {

  this: MailOwner =>

  override def sendEmail(recipients: util.List[String], subject: String, message: String, attachments: util.List[Attachment]): Unit = {
    val fromHeader = email()
    // todo:
  }
}
