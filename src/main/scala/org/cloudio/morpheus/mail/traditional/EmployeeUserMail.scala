package org.cloudio.morpheus.mail.traditional

/**
 * Created by zslajchrt on 24/08/15.
 */
trait EmployeeUserMail extends UserMail {
  this: Employee =>

  abstract override def sendEmail(message: Message): Unit = {
    val signature: String = "\n\n" + firstName + " " + lastName + "\n" + department

    super.sendEmail(message.copy(body = message.body + signature))
  }
}
