package org.cloudio.morpheus.mail.traditional

/**
 * Created by zslajchrt on 25/08/15.
 */
trait UserMail {

  def sendEmail(message: Message)

  def validateEmail(message: Message)

}
