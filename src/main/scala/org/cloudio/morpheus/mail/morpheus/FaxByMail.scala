package org.cloudio.morpheus.mail.morpheus

import org.morpheus._
import org.morpheus.Morpheus

/**
 * Created by zslajchrt on 26/08/15.
 */
@dimension
trait FaxByMail {
  def faxEmail(message: Message)
}

@fragment
trait DefaultFaxByMail extends FaxByMail {
  this: PremiumUser =>

  override def faxEmail(message: Message): Unit = {
    // todo
  }
}