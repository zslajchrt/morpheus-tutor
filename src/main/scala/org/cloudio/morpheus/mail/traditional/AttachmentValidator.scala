package org.cloudio.morpheus.mail.traditional

import java.util
import scala.collection.JavaConversions._

import org.cloudio.morpheus.mail.{Attachment, UserMail}

/**
 * Created by zslajchrt on 24/08/15.
 */
trait AttachmentValidator extends UserMail {

  abstract override def sendEmail(recipients: util.List[String], subject: String, message: String, attachments: util.List[Attachment]): Unit = {
    for (att <- attachments) {
      validateAttachment(att)
    }
    super.sendEmail(recipients, subject, message, attachments)
  }

  private def validateAttachment(att: Attachment): Unit = {
    // todo
  }
}
