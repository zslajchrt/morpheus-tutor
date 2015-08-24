package org.cloudio.morpheus.mail.traditional

import org.cloudio.morpheus.mail.MailOwner

/**
 * Created by zslajchrt on 24/08/15.
 */
trait RegisteredUserAdapter extends MailOwner {
  this: RegisteredUser =>

  override def isMale: Boolean = male
}
