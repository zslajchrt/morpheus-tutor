package org.cloudio.morpheus.mail.traditional

import java.util

import org.cloudio.morpheus.mail.{Attachment, UserMail}

/**
 * Created by zslajchrt on 24/08/15.
 */
trait EmployeeUserMail extends UserMail {
  this: Employee =>

  abstract override def sendEmail(recipients: util.List[String], subject: String, message: String, attachments: util.List[Attachment]): Unit = {
    val dep = department
    // ...
    super.sendEmail(recipients, subject, message, attachments)
  }
}
