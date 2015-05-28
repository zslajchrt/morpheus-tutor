package org.cloudio.morpheus.tutor.chat.frag.step1

import java.util.Locale

import org.morpheus._
import Morpheus._

/**
 * A plain mutable entity, no behavior.
 *
 * Created by zslajchrt on 04/05/15.
 */

@fragment
trait Contact {
  var firstName: String = _
  var lastName: String = _
  var male: Boolean = _
  var nationality: Locale = _
}

object Session {

  def main(args: Array[String]) {

    val contact = singleton[Contact].!
    //val contact = singleton[Contact].~

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    println(s"${contact.firstName} ${contact.lastName} ${contact.nationality} ${contact.male}")

  }
}
