package org.cloudio.morpheus.mail.morpheus

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@dimension @wrapper
trait VirusDetector extends UserMail {

  abstract override def validateEmail(message: Message): Unit = {
    validateAttachments(message)
    super.validateEmail(message)
  }

  private def validateAttachments(message: Message): Unit = {
    for (attachment <- message.attachments) {
      val result: String = scanAttachment(attachment)
      if (result != null) {
        throw new IllegalArgumentException("Virus found in attachment " + attachment + "\nDescription: " + result)
      }
    }
  }

  private def scanAttachment(attachment: Attachment): String = {
    // todo
    null
  }

 }
