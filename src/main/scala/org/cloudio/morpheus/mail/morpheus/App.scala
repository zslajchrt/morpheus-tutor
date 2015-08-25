package org.cloudio.morpheus.mail.morpheus

import org.morpheus.Morpheus._
import org.morpheus._


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {


  type MailMorphType = DefaultUserMail with ((RegisteredUserMail with RegisteredUserAdapter) or (EmployeeUserMail with EmployeeAdapter))
  //type MailMorphType = DefaultUserMail with ((RegisteredUserMail with RegisteredUserAdapter) or (EmployeeUserMail with EmployeeAdapter)) with /?[VirusDetector]
  //type MailMorphType = DefaultUserMail with ((RegisteredUserAdapter with RegisteredUserMail) or (EmployeeAdapter with EmployeeUserMail)) with /?[VirusDetector]
  val mailMorphModel = parse[MailMorphType](false)


  def main(args: Array[String]): Unit = {
    val user = singleton[Employee or RegisteredUser]

    //val userMail = initializeMailUser_1(user)
    val userMail = initializeMailUser_2(user)
    val userMailAVRef: &[UserMail with \?[$[VirusDetector]]] = *(userMail)
    val userMailAV = *(userMailAVRef, single[VirusDetector])

    val message = Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", List.empty[Attachment])

    val avRef: &[UserMail with VirusDetector] = userMailAV
    //val av = *(avRef).make
    val avKern = *(avRef)
    val av: UserMail = avKern.!
    av.validateEmail(message)
    av.sendEmail(message)

//    val av: VirusDetector = asMorphOf[VirusDetector](userMailAV)
//    av.sendEmail(message)

//    println(userMailAV.~.myAlternative)
//    println(userMailAV.~.isInstanceOf[VirusDetector])
//        userMailAV.~.sendEmail(message)
  }

  def initializeMailUser_1(user: &[$[MailMorphType]]) = {
    *(user, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail]).!
  }

  def initializeMailUser_2(userRef: &![Employee or RegisteredUser]): &[MailMorphType] = {
    val userExtRef: &[$[MailMorphType]] = *(userRef)
    *(userExtRef, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail])
  }


  object MailMorphStrategy {
    def apply(message: Message): MorphingStrategy[mailMorphModel.Model] = {
      promote[mailMorphModel.Model](Some(0))
    }
  }
}


