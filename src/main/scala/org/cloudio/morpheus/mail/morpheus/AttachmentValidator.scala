package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@dimension @wrapper
trait AttachmentValidator extends UserMail {

   abstract override def sendEmail(recipients: List[String], subject: String, message: String, attachments: List[Attachment]): Unit = {
     for (att <- attachments) {
       validateAttachment(att)
     }
     super.sendEmail(recipients, subject, message, attachments)
   }

   private def validateAttachment(att: Attachment): Unit = {
     // todo
   }
 }
