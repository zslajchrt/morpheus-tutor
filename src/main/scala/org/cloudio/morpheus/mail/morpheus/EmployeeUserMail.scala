package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment
import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@dimension @wrapper
trait EmployeeUserMail extends UserMail {
   this: Employee =>

   abstract override def sendEmail(message: Email): Unit = {
     val dep = employeeData.department
     // ...
     super.sendEmail(message)
   }
 }
