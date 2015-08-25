package org.cloudio.morpheus.mail.morpheus

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@dimension @wrapper
trait EmployeeUserMail extends UserMail {
   this: Employee =>

  abstract override def sendEmail(message: Message): Unit = {
    val signature: String = "\n\n" + employeeData.firstName + " " + employeeData.lastName + "\n" + employeeData.department

    super.sendEmail(message.copy(body = message.body + signature))
  }
 }
