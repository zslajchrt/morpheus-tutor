package org.cloudio.morpheus.mail.traditional

/**
 * Created by zslajchrt on 26/08/15.
 */
trait FaxByMail {
  def faxEmail(message: Message)
}

trait DefaultFaxByMail extends FaxByMail {
  this: PremiumUser =>

  override def faxEmail(message: Message): Unit = {
    // todo
  }
}