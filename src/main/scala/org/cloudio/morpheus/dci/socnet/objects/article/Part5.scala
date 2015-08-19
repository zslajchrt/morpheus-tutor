package org.cloudio.morpheus.dci.socnet.objects.article

import org.cloudio.morpheus.dci.socnet.networks.email._
import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 06/08/15.
 */
object Part5 {

  def main(args: Array[String]) {
    val regUserOrEmpModel = parse[(RegisteredUserEntity with RegisteredUserPublicCommon) or (EmployeeEntity with EmployeePublicCommon)](true)
    var failedFragments: Option[Set[Int]] = None
    val morphStrategy = MaskExplicitStrategy(rootStrategy(regUserOrEmpModel), true, () => failedFragments)
    val regUserOrEmpKernel = singleton(regUserOrEmpModel, morphStrategy)

    val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserDatasourcesMock]] = regUserOrEmpKernel
    val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
      single[UserDatasourcesMock])
    regUserOrEmpLoaderKernel.~.initSources("5")

    failedFragments = Some((for (loader <- regUserOrEmpLoaderKernel;
                                 loaderResult = loader.get.load
                                 if !loaderResult.succeeded;
                                 frag <- loaderResult.fragment) yield frag.index).toSet)

    ////
    // Mail service


    {
      val mailRef: &[$[MailService with
        FromHeaderValidator with
        /?[SignatureAppender] with
        \?[AttachmentValidator]]] = regUserOrEmpKernel

      val mailKernel = *(mailRef,
        single[MailServiceMock],
        single[FromHeaderValidator],
        single[SignatureAppender],
        single[AttachmentValidator])

      object MailServiceStrategy {

        def apply(msg: Message): MorphingStrategy[mailKernel.Model] = {
          val hasAtt: Option[Int] = if (msg.attachments.nonEmpty) Some(0) else None
          promote[AttachmentValidator](rootStrategy(mailKernel.model), hasAtt)
        }
      }

      def sendMail(msg: Message): Unit = {
        val m = mailKernel.~.remorph(MailServiceStrategy(msg))
        println(m.myAlternative)
        m.send(msg)
      }

      sendMail(Message(None, List("agata@gmail.com"), "Hello", "Bye", Nil))
      sendMail(Message(None, List("agata@gmail.com"), "Hello", "Bye", List(Attachment("att1", Array[Byte](0,1,2), "mime1"))))
    }

  }
}
