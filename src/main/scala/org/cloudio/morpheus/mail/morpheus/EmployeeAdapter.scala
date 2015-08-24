package org.cloudio.morpheus.mail.morpheus

import java.util.Date

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 24/08/15.
 */
@fragment
trait EmployeeAdapter extends MailOwner {
  this: Employee =>

  override def nick: String = employeeData.employeeCode

  override def email: String = employeeData.employeeCode + "@bigcompany.com"

  override def birthDate: Date = employeeData.birth

  override def firstName: String = employeeData.firstName

  override def lastName: String = employeeData.lastName

  override def isMale: Boolean = employeeData.isMale
}
