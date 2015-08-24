package org.cloudio.morpheus.mail.morpheus

import java.util.Date

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 24/08/15.
 */
case class RegisteredUserData(
                           nick: String,

                           firstName: String,

                           lastName: String,

                           email: String,

                           male: Boolean,

                           birthDate: Date,

                           premium: Boolean,

                           validFrom: Date,

                           validTo: Date
                           )

@fragment
trait RegisteredUser {
  var regUserData: RegisteredUserData = _
}