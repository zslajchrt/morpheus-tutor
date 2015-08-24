package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment
import org.morpheus.fragment

/**
 * Created by zslajchrt on 24/08/15.
 */
@fragment
trait DefaultUserMail extends UserMail {

  this: MailOwner =>

  override def sendEmail(recipients: List[String], subject: String, message: String, attachments: List[Attachment]): Unit = {
    val fromHeader = email
    // todo:
  }
}
