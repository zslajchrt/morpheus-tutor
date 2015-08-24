package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 24/08/15.
 */
@dimension
trait UserMail {

  def sendEmail(message: Email)
}


case class Email(recipients: List[String], subject: String, message: String, attachments: List[Attachment])