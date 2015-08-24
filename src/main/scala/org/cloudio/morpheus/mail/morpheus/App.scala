package org.cloudio.morpheus.mail.morpheus

import org.cloudio.morpheus.mail.Attachment
import org.morpheus._
import org.morpheus.Morpheus._


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  type MailMorphType = DefaultUserMail with
    ((RegisteredUserAdapter with RegisteredUserMail) or (EmployeeAdapter with EmployeeUserMail)) with
    \?[AttachmentValidator]
  val mailMorphModel = parse[MailMorphType](false)

  def main(args: Array[String]): Unit = {
    val user = singleton[Employee or RegisteredUser]

    val userMail = initializeMailUser_1(user)
    //val userMail = initializeMailUser_2(user)

    val message = Email(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", List.empty[Attachment])
    userMail.remorph(MailMorphStrategy(message)).sendEmail(message)
  }

  def initializeMailUser_1(user: &[$[MailMorphType]]) = {

    *(user, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[AttachmentValidator]).!
  }

  def initializeMailUser_2(userRef: &![Employee or RegisteredUser]) = {
    val userExtRef: &[$[MailMorphType]] = *(userRef)

    *(userExtRef, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[AttachmentValidator]).!
  }


  object MailMorphStrategy {
    def apply(message: Email): MorphingStrategy[mailMorphModel.Model] = {
      promote(rootStrategy(mailMorphModel), Some(0))
    }
  }
}


