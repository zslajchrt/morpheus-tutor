package org.cloudio.morpheus.mail.morpheus

import java.util.Date

import org.morpheus._

/**
 * Created by zslajchrt on 24/08/15.
 */

case class EmployeeData(
                         firstName: String,

                         middleName: String,

                         lastName: String,

                         title: String,

                         isMale: Boolean,

                         birth: Date,

                         employeeCode: String,

                         position: String,

                         department: String

                         )

@fragment
trait Employee {
  var employeeData: EmployeeData = _
}