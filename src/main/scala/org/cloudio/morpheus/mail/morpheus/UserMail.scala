package org.cloudio.morpheus.mail.morpheus

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 24/08/15.
 */
@dimension
trait UserMail {

  def sendEmail(message: Message)

  def validateEmail(message: Message)
}

case class Attachment(name: String, data: Array[Byte], mime: String)

case class Message(recipients: List[String], subject: String, body: String, attachments: List[Attachment])