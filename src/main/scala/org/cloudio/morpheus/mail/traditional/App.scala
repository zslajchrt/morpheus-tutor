package org.cloudio.morpheus.mail.traditional

import java.util
import java.util.{Collections, Arrays}

import org.cloudio.morpheus.mail.{Attachment, UserMail}


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  def main(args: Array[String]): Unit = {
    val userMail = initializeMailUser(null)
    userMail.sendEmail(util.Arrays.asList("pepa@gmail.com"), "Hello", "Hi, Pepa!", Collections.emptyList[Attachment])
  }

  def initializeMailUser(user: Any): UserMail = {
    user match {
      case ru: RegisteredUser =>
        val ruMail = new RegisteredUser() with
          RegisteredUserAdapter with
          DefaultUserMail with
          RegisteredUserMail with
          AttachmentValidator

        ruMail.adoptState(ru)
        ruMail

      case emp: Employee =>
        val empMail = new Employee() with
          EmployeeAdapter with
          DefaultUserMail with
          EmployeeUserMail with
          AttachmentValidator

        empMail.adoptState(emp)
        empMail
    }
  }

}
