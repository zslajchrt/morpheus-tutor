package org.cloudio.morpheus.mail.traditional

import java.util.Date

/**
 * Created by zslajchrt on 24/08/15.
 */
class RegisteredUser {
  var nick: String = _
  var firstName: String = _
  var lastName: String = _
  var email: String = _
  var male: Boolean = _
  var birthDate: Date = _
  var premium: Boolean = _
  var validFrom: Date = _
  var validTo: Date = _

  def adoptState(other: RegisteredUser): Unit = {
    nick = other.nick
    firstName = other.firstName
    lastName = other.lastName
    email = other.email
    male = other.male
    birthDate = other.birthDate
    premium = other.premium
    validFrom = other.validFrom
    validTo = other.validTo
  }
}
