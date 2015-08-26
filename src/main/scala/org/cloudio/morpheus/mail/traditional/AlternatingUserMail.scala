package org.cloudio.morpheus.mail.traditional


/**
 * Created by zslajchrt on 26/08/15.
 */
class AlternatingUserMail(userMail1: UserMail, userMail2: UserMail) extends UserMail {

  private var left: Boolean = true

  override def sendEmail(message: Message) {
    getDelegate.sendEmail(message)
  }

  override def validateEmail(message: Message) {
    getDelegate.validateEmail(message)
  }

  def setCurrent(left: Boolean) {
    this.left = left
  }

  private def getDelegate: UserMail = if (left) userMail1 else userMail2

  def canFaxEmail(message: Message): Boolean = getDelegate.isInstanceOf[FaxByMail]

  def faxEmail(message: Message) {
    getDelegate match {
      case fax: FaxByMail =>
        fax.faxEmail(message)
      case _ =>
        throw new IllegalStateException("The current service does not support fax")
    }
  }
}
