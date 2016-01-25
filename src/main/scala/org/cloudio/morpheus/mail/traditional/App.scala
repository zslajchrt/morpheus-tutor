package org.cloudio.morpheus.mail.traditional

import java.util.{Date, Calendar}


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  def main(args: Array[String]): Unit = {
    val userMail = initializeMailUser(null, null)
    userMail.sendEmail(Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", Nil))

    userMail match {
      case r: RegisteredUser with PremiumUser =>
        //...
    }

  }

  def initializeMailUser(employee: Employee, registeredUser: RegisteredUser): UserMail = {

    val employeeMail = new Employee() with
      EmployeeAdapter with
      DefaultUserMail with
      EmployeeUserMail with
      VirusDetector

    employeeMail.adoptState(employee)

    val regUserMail = new RegisteredUser() with
      RegisteredUserAdapter with
      DefaultUserMail with
      RegisteredUserMail with
      VirusDetector
    regUserMail.adoptState(registeredUser)

    val regUserMailPremium = new RegisteredUser() with PremiumUser with
      RegisteredUserAdapter with
      DefaultUserMail with
      RegisteredUserMail with
      VirusDetector with
      DefaultFaxByMail
    regUserMailPremium.adoptState(registeredUser)

    // We still need to clone the state of both employee and registeredUser instances

    // EmployeeUserMail and RegisteredUserMail are now more general since they extend UserMail and not DefaultUserMail

    // VirusDetector no longer needs to override sendEmail; it validly extends DefaultUserMail; no schizophrenia.

    // These adoption methods are annoying.

    new AlternatingUserMail {
      override protected def getDelegate: UserMail = {
        val c = Calendar.getInstance()
        def h = c.get(Calendar.HOUR_OF_DAY)
        if (h >= 8 && h < 17) {
          getEmployeeMail
        } else {
          getRegUserMail
        }
      }

      def getEmployeeMail = {
        employeeMail
      }

      def getRegUserMail = {
        if (registeredUser.premium &&
          registeredUser.validTo != null &&
          registeredUser.validTo.after(new Date()))
          regUserMailPremium
        else
          regUserMail
      }

    }

    // The client must be fixed to AlternatingUserMail through which it can determine whether the service supports fax.

    // VirusDetector trait is specified twice; this duplicity may cause some problems:
    // 1) the virusCounter will exist in two copies, which may cause problems when monitoring the counter, for instance.
    // 2) it may use more system resources
    // 3) error-prone when refactoring the source code; easy to omit a VirusDetector's occurrence
    // VirusDetector cannot be used as a trait of AlternatingUserMail, which could be suggested as a solution, since its
    // validateEmail method would not be invoked from DefaultUserMail.sendEmail because of the delegation.

    // The resulting instance still does not allow determining the user account type from the instance's type.
  }

}
