package org.cloudio.morpheus.dci.socnet.networks.email

import org.cloudio.morpheus.dci.socnet.objects.Person.PersonType
import org.cloudio.morpheus.dci.socnet.objects.{PersonSample, PersonPublicEntity, PersonConnectionsEntity}
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/07/15.
 */

case class Attachment(name: String, data: Array[Byte], mime: String)

case class Message(from: String, recipients: List[String], subject: String, message: String, attachments: List[Attachment])

@dimension
trait MailService {
  def send(message: Message): Unit
}

@dimension
@wrapper
trait AttachmentValidator extends MailService {

  abstract override def send(message: Message): Unit = {
    checkAttachments(message.attachments)

    super.send(message)
  }

  def checkAttachments(attachments: List[Attachment]): Unit = {
    // todo
  }

}

@dimension
@wrapper
trait AdAppender extends MailService {

  this: AdSelector =>

  abstract override def send(msg: Message): Unit = {
    val ad = selectAd(msg)
    val msgWithAd = s"${msg.message}\n$ad"

    super.send(msg.copy(message = msgWithAd))
  }

}

@dimension
trait AdSelector {
  def selectAd(message: Message): String
}

// Mocks

@fragment
trait MailServiceMock extends MailService {
  override def send(message: Message): Unit = {
    println(s"Sending message:\n$message")
  }
}

@fragment
trait AdSelectorMock extends AdSelector {
  override def selectAd(message: Message): String = "Ad Mock"
}

@fragment
trait AdSelectorMock2 extends AdSelector {
  override def selectAd(message: Message): String = "Ad Mock2"
}

// Assemblage

object MailServiceAsm {

  type ModelType = $[MailServiceMock]
    with \?[$[AdAppender]
    with ($[AdSelectorMock] or $[AdSelectorMock2])]
    with $[AttachmentValidator]
    with PersonPublicEntity

  //val model = parseRef[ModelType]
  //val kernel = singleton(model, rootStrategy(model))

  object MailServiceStrategy {

    def apply(msg: Message) = {
      //promote[\?[AdAppender with (AdSelectorMock or AdSelectorMock2)]](kernel.defaultStrategy, 2)
//      val hasAd: Option[Int] = if (msg.message.length > 10) Some(0) else None
//      val adType = for (ha <- hasAd) yield if (msg.attachments.isEmpty) 0 else 1
//
//      val s1 = promote[AdAppender](kernel.defaultStrategy, hasAd)
//      val s2 = promote[AdSelectorMock or AdSelectorMock2](s1, adType)
//
//      s2
      null
    }

  }

  def main(args: Array[String]) {

    val user = PersonSample.personsAsMap("joe1")
    val mailRef: &![ModelType] = user

//    val user = PersonSample.personsAsMap("joe1")
//    //val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "Bye", Nil)
//    //val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "ByeASDSADSADASDSADASDSADASDASDASDSDAASDSA", Nil)
//    val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "ByeASDSADSADASDSADASDSADASDASDASDSDAASDSA", List(Attachment("att1", Array[Byte](0,1,2), "mime1")))
//    val morph = kernel.morph_~(MailServiceStrategy(msg))
//    morph.send(msg)
//    println(s"\nUsing alternative: ${morph.myAlternative}")

  }


}