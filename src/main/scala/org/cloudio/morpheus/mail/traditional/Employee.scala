package org.cloudio.morpheus.mail.traditional

import java.util.Date

/**
 * Created by zslajchrt on 24/08/15.
 */
class Employee {
  var firstName: String = null
  var middleName: String = null
  var lastName: String = null
  var title: String = null
  var isMale: Boolean = false
  var birth: Date = null
  var employeeCode: String = null
  var position: String = null
  var department: String = null

  def adoptState(other: Employee): Unit = {
    firstName = other.firstName
    middleName = other.middleName
    lastName = other.lastName
    title = other.title
    isMale = other.isMale
    birth = other.birth
    employeeCode = other.employeeCode
    position = other.position
    department = other.department
  }
}
