package org.cloudio.morpheus.mail.traditional

import java.util.Date

import org.cloudio.morpheus.mail.MailOwner

/**
 * Created by zslajchrt on 24/08/15.
 */
trait EmployeeAdapter extends MailOwner {
  this: Employee =>

  override def nick(): String = employeeCode

  override def email(): String = employeeCode + "@bigcompany.com"

  override def birthDate(): Date = birth
}
