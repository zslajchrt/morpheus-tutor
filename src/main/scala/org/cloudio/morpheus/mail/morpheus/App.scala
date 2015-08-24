package org.cloudio.morpheus.mail.morpheus

import java.util

import org.cloudio.morpheus.mail.Attachment
import org.morpheus._
import org.morpheus.Morpheus._


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  def main(args: Array[String]): Unit = {
    val user = singleton[Employee or RegisteredUser]
    println(user.!.myAlternative)

    val userMail = initializeMailUser(user)
    userMail.sendEmail(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", List.empty[Attachment])
  }

  def initializeMailUser_1(user: &[Employee or RegisteredUser]): UserMail = {
    null
  }


  def initializeMailUser(user: &[$[DefaultUserMail with
    ((RegisteredUserAdapter with RegisteredUserMail) or
      (EmployeeAdapter with EmployeeUserMail)) with \?[AttachmentValidator]]]): UserMail = {

    val ret = *(user, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[AttachmentValidator]).!
    println(ret.myAlternative)
    ret
    //     user match {
    //       case ru: RegisteredUser =>
    //         val ruMail = new RegisteredUser() with
    //           RegisteredUserAdapter with
    //           DefaultUserMail with
    //           RegisteredUserMail with
    //           AttachmentValidator
    //
    //         ruMail.adoptState(ru)
    //         ruMail
    //
    //       case emp: Employee =>
    //         val empMail = new Employee() with
    //           EmployeeAdapter with
    //           DefaultUserMail with
    //           EmployeeUserMail with
    //           AttachmentValidator
    //
    //         empMail.adoptState(emp)
    //         empMail
    //     }
  }

}
