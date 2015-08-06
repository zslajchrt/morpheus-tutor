package org.cloudio.morpheus.dci.socnet.objects.article

import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus._
import org.morpheus.Morpheus._
import org.morpheus.FragmentValidator._

/**
 * Created by zslajchrt on 06/08/15.
 */
object Part1 {

  def main(args: Array[String]) {
    val regUserKernel = singleton[RegisteredUserEntity]
    println(regUserKernel.~.registeredUser)

    {
      val regUserLoaderRef: &[$[RegisteredUserLoader with UserProtodataMock]] = regUserKernel
      val regUserLoaderKernel = *(regUserLoaderRef, single[RegisteredUserLoader], single[UserProtodataMock])
      regUserLoaderKernel.~.initSources("4")
      regUserLoaderKernel.~.load match {
        case Failure(_, reason) =>
          sys.error(s"Cannot load registered user: $reason")
        case Success(_) =>
          println(regUserKernel.~.registeredUser)

      }

    }
    //////
    {
      val empKernel = singleton[EmployeeEntity]
      val empLoaderRef: &[$[EmployeeLoader with UserProtodataMock]] = empKernel
      // ...
    }

  }
}
