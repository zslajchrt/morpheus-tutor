package org.cloudio.morpheus.dci.socnet.objects.article

import org.cloudio.morpheus.dci.socnet.networks.email._
import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 06/08/15.
 */
object Part4 {

  def main(args: Array[String]) {
    val regUserOrEmpModel = parse[PersonPublicCommon with (RegisteredUserEntity or EmployeeEntity)](true)
    var failedFragments: Option[Set[Int]] = None
    val morphStrategy = MaskExplicitStrategy(rootStrategy(regUserOrEmpModel), true, () => failedFragments)
    val regUserOrEmpKernel = singleton(regUserOrEmpModel, morphStrategy)

    val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserProtodataMock]] = regUserOrEmpKernel
    val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
      single[UserProtodataMock])
    regUserOrEmpLoaderKernel.~.initSources("5")

    failedFragments = Some((for (loader <- regUserOrEmpLoaderKernel;
                                 loaderResult = loader.load
                                 if !loaderResult.succeeded;
                                 frag <- loaderResult.fragment) yield frag.index).toSet)

    val publicPerson = asMorphOf[PersonPublicCommon](regUserOrEmpKernel.~)
    println(publicPerson.personData)
    println(s"${publicPerson.nick}, ${publicPerson.firstName}, ${publicPerson.lastName}, ${publicPerson.email}")


    ////
    // Mail service


    {
//      val mailRef: &[$[MailService]] = regUserOrEmpKernel
//      val mailKernel = *(mailRef, single[MailServiceMock])
//      val mailRef: &[$[MailService with FromHeaderValidator]] = regUserOrEmpKernel
//      val mailKernel = *(mailRef, single[MailServiceMock], single[FromHeaderValidator])
      val mailRef: &[$[MailService with FromHeaderValidator with /?[SignatureAppender]]] = regUserOrEmpKernel
      // todo: describe the effect of \?[F] and /?[F] on choosing the winning alternative
      val mailKernel = *(mailRef, single[MailServiceMock], single[FromHeaderValidator], single[SignatureAppender])
      val alts0 = mailKernel.model.alternatives

      val msg = Message(None, List("agata@gmail.com"), "Hello", "Bye", Nil)
      val alternatives = mailKernel.~.alternatives
      println(alternatives.toList)
      println(alternatives.toMaskedList)
      println(mailKernel.~.myAlternative)
      mailKernel.~.send(msg)
    }

  }
}
