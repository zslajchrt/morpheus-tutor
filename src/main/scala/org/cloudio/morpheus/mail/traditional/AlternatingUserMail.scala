package org.cloudio.morpheus.mail.traditional


/**
 * Created by zslajchrt on 26/08/15.
 */
abstract class AlternatingUserMail extends UserMail {

  override def sendEmail(message: Message) {
    getDelegate.sendEmail(message)
  }

  override def validateEmail(message: Message) {
    getDelegate.validateEmail(message)
  }

  protected def getDelegate: UserMail

  def canFaxEmail: Boolean = getDelegate.isInstanceOf[FaxByMail]

  def faxEmail(message: Message) {
    getDelegate match {
      case fax: FaxByMail =>
        fax.faxEmail(message)
      case _ =>
        throw new IllegalStateException("The current service does not support fax")
    }
  }
}
