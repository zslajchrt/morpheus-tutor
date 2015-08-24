package org.cloudio.morpheus.mail.morpheus

import java.util.Date

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 24/08/15.
  */
@fragment
trait RegisteredUserAdapter extends MailOwner {
   this: RegisteredUser =>

  override def isMale: Boolean = regUserData.male

  override def nick: String = regUserData.nick

  override def lastName: String = regUserData.lastName

  override def email: String = regUserData.email

  override def birthDate: Date = regUserData.birthDate

  override def firstName: String = regUserData.firstName
}
