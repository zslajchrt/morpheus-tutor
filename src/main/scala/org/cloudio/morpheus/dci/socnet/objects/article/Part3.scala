package org.cloudio.morpheus.dci.socnet.objects.article

import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 06/08/15.
 */
object Part3 {

  def main(args: Array[String]) {
    val regUserOrEmpModel = parse[RegisteredUserEntity or EmployeeEntity](true)
    var failedFragments: Option[Set[Int]] = None
    val morphStrategy = MaskExplicitStrategy(rootStrategy(regUserOrEmpModel), true, () => failedFragments)
    val regUserOrEmpKernel = singleton(regUserOrEmpModel, morphStrategy)

    val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserProtodataMock]] = regUserOrEmpKernel
    val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
      single[UserProtodataMock])
    regUserOrEmpLoaderKernel.~.initSources("6")

    failedFragments = Some((for (loader <- regUserOrEmpLoaderKernel;
                                 loaderResult = loader.load
                                 if !loaderResult.succeeded;
                                 frag <- loaderResult.fragment) yield frag.index).toSet)

    //      val regUserEnt = asMorphOf[RegisteredUserEntity](regUserOrEmpKernel)
    //      println(regUserEnt.registeredUser)
    //      val empEnt = asMorphOf[EmployeeEntity](regUserOrEmpKernel)
    //      println(empEnt.employee)

    try {
      regUserOrEmpKernel.~.remorph
      select[RegisteredUserEntity](regUserOrEmpKernel.~) match {
        case Some(regUserEnt) =>
          println(regUserEnt.registeredUser)
        case None =>
          select[EmployeeEntity](regUserOrEmpKernel.~) match {
            case Some(empEnt) => println(empEnt.employee)
            case None => require(false)
          }
      }

    }
    catch {
      case ae: NoViableAlternativeException =>
        println("Cannot load registered user or employee")
    }

  }
}
