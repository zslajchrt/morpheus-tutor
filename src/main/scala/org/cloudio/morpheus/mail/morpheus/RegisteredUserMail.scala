package org.cloudio.morpheus.mail.morpheus

import java.util.Date
import org.morpheus._
import org.morpheus.Morpheus._

import org.cloudio.morpheus.mail.Attachment

/**
 * Created by zslajchrt on 24/08/15.
 */
@dimension @wrapper
trait RegisteredUserMail extends UserMail {
  this: RegisteredUser =>

  abstract override def validateEmail(message: Message) {
    val now: Date = new Date
    if (regUserData.validTo.compareTo(now) < 0) {
      throw new IllegalArgumentException("User's account expired")
    }
    super.validateEmail(message)
  }
}
