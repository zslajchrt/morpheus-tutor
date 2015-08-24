package org.cloudio.morpheus.mail.morpheus

import java.util.Date

import org.morpheus.dimension

/**
 * Created by zslajchrt on 24/08/15.
 */
trait MailOwner {
  def nick: String

  def firstName: String

  def lastName: String

  def email: String

  def isMale: Boolean

  def birthDate: Date
}
