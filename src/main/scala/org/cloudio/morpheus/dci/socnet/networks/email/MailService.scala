package org.cloudio.morpheus.dci.socnet.networks.email

import java.util.Date

import org.cloudio.morpheus.dci.socnet.objects.Person.PersonType
import org.cloudio.morpheus.dci.socnet.objects._
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
    println(s"Checked ${attachments.size} attachments")
  }

}

@dimension
@wrapper
trait AdAppender extends MailService {

  this: PersonAdStatsEntity with AdSelector =>

  abstract override def send(msg: Message): Unit = {
    val ad = selectAd(msg)
    val msgWithAd = s"${msg.message}\n$ad"

    addSeenAd(ad)

    super.send(msg.copy(message = msgWithAd))
  }

}

@dimension
trait AdSelector {
  def selectAd(message: Message): Ad
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
  override def selectAd(message: Message) = Ad("Ad Mock", "http://abc", new Date, keywords = List("fun"))
}

@fragment
trait AdSelectorMock2 extends AdSelector {
  override def selectAd(message: Message) = Ad("Ad Mock2", "http://xyz", new Date, keywords = List("fun", "holiday"))
}

// Assemblage

object MailServiceAsm {

  type ModelType = MailServiceMock
    with \?[AttachmentValidator]
    with \?[AdAppender with (AdSelectorMock or AdSelectorMock2)]

  val mailServiceKernel = singleton_?[ModelType]
  val mailServiceFragments = tupled(mailServiceKernel)

  object MailServiceStrategy {

    def apply(msg: Message) = {
      val hasAtt: Option[Int] = if (msg.attachments.nonEmpty) Some(0) else None
      val hasAd: Option[Int] = if (msg.message.length > 10) Some(0) else None
      val adSelType = for (ha <- hasAd) yield if (msg.attachments.isEmpty) 0 else 1

      val s1 = promote[AttachmentValidator](rootStrategy(mailServiceKernel.model), hasAtt)
      val s2 = promote[AdAppender](s1, hasAd)
      promote[AdSelectorMock or AdSelectorMock2](s2, adSelType)
    }
  }

  def main(args: Array[String]) {
    //val t = mailServiceKernel.tupled

    val user = PersonSample.personsAsMap("joe1")

    //val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "Bye", Nil)
    //val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "ByeASDSADSADASDSADASDSADASDASDASDSDAASDSA", Nil)
    val msg = Message(user.!.email, List("agata@gmail.com"), "Hello", "ByeASDSADSADASDSADASDSADASDASDASDSDAASDSA", List(Attachment("att1", Array[Byte](0,1,2), "mime1")))

    val mailRef: &[$[ModelType]] = user
    val mailKernel = *(mailRef, MailServiceStrategy(msg), mailServiceFragments)
    mailKernel.!.send(msg)
    println(s"\nUsing alternative: ${mailKernel.!.myAlternative}")

  }


}