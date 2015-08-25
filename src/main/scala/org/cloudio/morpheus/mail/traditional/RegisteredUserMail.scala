package org.cloudio.morpheus.mail.traditional

import java.util.Date


/**
 * Created by zslajchrt on 24/08/15.
 */
trait RegisteredUserMail extends UserMail {
  this: RegisteredUser =>

  abstract override def validateEmail(message: Message) {
    val now: Date = new Date
    if (validTo.compareTo(now) < 0) {
      throw new IllegalArgumentException("User's account expired")
    }
    super.validateEmail(message)
  }
}
