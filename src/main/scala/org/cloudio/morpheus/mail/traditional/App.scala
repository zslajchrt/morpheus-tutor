package org.cloudio.morpheus.mail.traditional



/**
 * Created by zslajchrt on 24/08/15.
 */
object App {

  def main(args: Array[String]): Unit = {
    val userMail = initializeMailUser(null)
    userMail.sendEmail(Message(List("pepa@gmail.com"), "Hello", "Hi, Pepa!", Nil))
  }

  def initializeMailUser(user: Any): UserMail = {
    user match {
      case ru: RegisteredUser =>
        val ruMail = new RegisteredUser() with
          RegisteredUserAdapter with
          DefaultUserMail with
          RegisteredUserMail with
          VirusDetector

        ruMail.adoptState(ru)
        ruMail

      case emp: Employee =>
        val empMail = new Employee() with
          EmployeeAdapter with
          DefaultUserMail with
          EmployeeUserMail with
          VirusDetector

        empMail.adoptState(emp)
        empMail
    }
  }

}
