package org.cloudio.morpheus.mail.traditional


/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  def main(args: Array[String]): Unit = {
    val userMail = initializeMailUser(null, null)
    userMail.sendEmail(Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", Nil))
  }

  def initializeMailUser(employee: Employee, registeredUser: RegisteredUser): UserMail = {

    val empMail = new Employee() with
      EmployeeAdapter with
      DefaultUserMail with
      EmployeeUserMail with
      VirusDetector

    empMail.adoptState(employee)

    val ruMail = if (registeredUser.premium)
      new RegisteredUser() with
        RegisteredUserAdapter with
        DefaultUserMail with
        RegisteredUserMail with
        VirusDetector with
        DefaultFaxByMail
    else
      new RegisteredUser() with
        RegisteredUserAdapter with
        DefaultUserMail with
        RegisteredUserMail with
        VirusDetector

    ruMail.adoptState(registeredUser)

    // We still need to clone the state of both employee and registeredUser instances

    // EmployeeUserMail and RegisteredUserMail are now more general since they extend UserMail and not DefaultUserMail

    // VirusDetector no longer needs to override sendEmail; it validly extends DefaultUserMail; no schizophrenia.

    // These adoption methods are annoying.

    new AlternatingUserMail(empMail, ruMail)
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
