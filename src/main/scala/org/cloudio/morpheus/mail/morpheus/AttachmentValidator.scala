package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@dimension @wrapper
trait AttachmentValidator extends UserMail {

   abstract override def sendEmail(message: Email): Unit = {
     for (att <- message.attachments) {
       validateAttachment(att)
     }
     super.sendEmail(message)
   }

   private def validateAttachment(att: Attachment): Unit = {
     // todo
   }
 }
