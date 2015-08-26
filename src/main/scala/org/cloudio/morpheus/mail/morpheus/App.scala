package org.cloudio.morpheus.mail.morpheus

import org.morpheus.Morpheus._
import org.morpheus._


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {


  type MailMorphType = DefaultUserMail with ((RegisteredUserMail with RegisteredUserAdapter with \?[DefaultFaxByMail]) or (EmployeeUserMail with EmployeeAdapter))
  //type MailMorphType = DefaultUserMail with ((RegisteredUserMail with RegisteredUserAdapter) or (EmployeeUserMail with EmployeeAdapter)) with /?[VirusDetector]
  //type MailMorphType = DefaultUserMail with ((RegisteredUserAdapter with RegisteredUserMail) or (EmployeeAdapter with EmployeeUserMail)) with /?[VirusDetector]
  val mailMorphModel = parse[MailMorphType](false)


  def main(args: Array[String]): Unit = {
    val user = singleton[Employee or RegisteredUser]
    val userMailRef: &[$[MailMorphType]] = user
    val userMail = *(userMailRef, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail], single[DefaultFaxByMail])
    val userMailAVRef: &[UserMail with \?[$[VirusDetector]]] = userMail
    val userMailAV = *(userMailAVRef, single[VirusDetector])

    val message = Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", List.empty[Attachment])

    val avRef: &[UserMail with VirusDetector] = userMailAV
    //val av = *(avRef).make
    val avKern = *(avRef)
    val av: UserMail = avKern.!
    av.validateEmail(message)
    av.sendEmail(message)

    // No need for AlternatingUserMail, since the platform provides such a functionality.

    val av2 = asMorphOf[VirusDetector](userMailAV)
    av2.sendEmail(message)

//    println(userMailAV.~.myAlternative)
//    println(userMailAV.~.isInstanceOf[VirusDetector])
//        userMailAV.~.sendEmail(message)
  }

//  def initializeMailUser_1(user: &[$[MailMorphType]]) = {
//    *(user, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail]).!
//  }
//
//  def initializeMailUser_2(userRef: &![Employee or RegisteredUser]): &[MailMorphType] = {
//    val userExtRef: &[$[MailMorphType]] = *(userRef)
//    *(userExtRef, single[DefaultUserMail], single[RegisteredUserAdapter], single[RegisteredUserMail], single[EmployeeAdapter], single[EmployeeUserMail])
//  }


  object MailMorphStrategy {
    def apply(message: Message): MorphingStrategy[mailMorphModel.Model] = {
      promote[mailMorphModel.Model](Some(0))
    }
  }
}


