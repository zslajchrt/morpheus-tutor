package org.cloudio.morpheus.mail.morpheus

import org.morpheus.Morpheus._
import org.morpheus._


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {


  type MailMorphType = DefaultUserMail with ((RegisteredUserMail with RegisteredUserAdapter) or (EmployeeUserMail with EmployeeAdapter)) with /?[VirusDetector]
  //type MailMorphType = DefaultUserMail with ((RegisteredUserAdapter with RegisteredUserMail) or (EmployeeAdapter with EmployeeUserMail)) with /?[VirusDetector]
  val mailMorphModel = parse[MailMorphType](false)


  def main(args: Array[String]): Unit = {
    val user = singleton[Employee or RegisteredUser]

    val userMail = initializeMailUser_1(user)
    //val userMail = initializeMailUser_2(user)

    val message = Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", List.empty[Attachment])
    val remorphed = userMail.remorph(MailMorphStrategy(message))
    println(remorphed.myAlternative)
    println(remorphed.isInstanceOf[VirusDetector])
    remorphed.sendEmail(message)
  }

  def initializeMailUser_1(user: &[$[MailMorphType]]) = {
    *(user, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[VirusDetector]).!
  }

  def initializeMailUser_2(userRef: &![Employee or RegisteredUser]) = {
    val userExtRef: &[$[MailMorphType]] = *(userRef)

    *(userExtRef, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[VirusDetector]).!
  }


  object MailMorphStrategy {
    def apply(message: Message): MorphingStrategy[mailMorphModel.Model] = {
      promote[mailMorphModel.Model](Some(0))
    }
  }
}


