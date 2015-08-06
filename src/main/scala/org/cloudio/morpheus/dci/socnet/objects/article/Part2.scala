package org.cloudio.morpheus.dci.socnet.objects.article

import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus.FragmentValidator._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 06/08/15.
 */
object Part2 {

  def main(args: Array[String]) {
    val regUserOrEmpKernel = singleton[RegisteredUserEntity or EmployeeEntity]
    val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserProtodataMock]] = regUserOrEmpKernel
    val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
      single[UserProtodataMock])
    regUserOrEmpLoaderKernel.~.initSources("5")

    for (loader <- regUserOrEmpLoaderKernel) {
      println(loader.myAlternative)
      loader.load
    }

    val regUserEnt = asMorphOf[RegisteredUserEntity](regUserOrEmpKernel)
    println(regUserEnt.registeredUser)
    val empEnt = asMorphOf[EmployeeEntity](regUserOrEmpKernel)
    println(empEnt.employee)
  }
}
