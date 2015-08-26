package org.cloudio.morpheus.mail.traditional

import org.cloudio.morpheus.mail.MailOwner

/**
 * Created by zslajchrt on 26/08/15.
 */
trait FaxByMail {
  def faxEmail(message: Message)
}

trait DefaultFaxByMail extends FaxByMail {
  this: MailOwner =>

  override def faxEmail(message: Message): Unit = {
    // todo
  }
}