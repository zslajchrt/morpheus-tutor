package org.cloudio.morpheus.mail.traditional

/**
 * Created by zslajchrt on 25/08/15.
 */
case class Message(recipients: List[String], subject: String, body: String, attachments: List[Attachment])