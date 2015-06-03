package org.cloudio.morpheus.tutor.chat.frag.step9

import java.util.Locale

import org.cloudio.morpheus.tutor.chat.frag.step8.{NewLineAppender, ChannelMonitor}
import org.morpheus._
import Morpheus._

import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * Fragment wrappers
 *
 * Created by zslajchrt on 05/05/15.
 */

trait BufferWatchDogConfig {
  val bufferWatchDogLimit: Int
}

case class DefaultBufferWatchDogConfig(bufferWatchDogLimit: Int) extends BufferWatchDogConfig

@fragment @wrapper
trait BufferWatchDog extends MemoryOutputChannel with dlg[BufferWatchDogConfig] {

  override def printText(text: String): Unit = {
    if (outputBuffer.size + text.length > bufferWatchDogLimit) {
      throw new IllegalStateException("Full")
    }
    super.printText(text)
  }

}

object Session {

  def main(args: Array[String]) {

    val contactData = ContactData("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)
    implicit val bufWDFrag = single[BufferWatchDog, BufferWatchDogConfig](DefaultBufferWatchDogConfig(10))

    val contactKernel = singleton[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or (MemoryOutputChannel with \?[BufferWatchDog])) with
      \?[NewLineAppender] with
      \?[ChannelMonitor]]

    var contactCoord: Int = 1
    val contactStr = promote[OfflineContact or OnlineContact](
      RootStrategy[contactKernel.Model](), contactCoord)

    var printerCoord: Int = 1
    val printerStr = promote[ContactRawPrinter or ContactPrettyPrinter](
      contactStr, printerCoord)

    var channelCoord: Int = 1
    val channelStr = promote[StandardOutputChannel or MemoryOutputChannel](
      printerStr, channelCoord)

    var bufferCoord: Int = 1
    val bufferWatchDogStr = promote[\?[BufferWatchDog]](channelStr, bufferCoord)

    var channelWrappersCoord: Int = 3
    val wrappersStr = promote[\?[ChannelMonitor] with \?[NewLineAppender]](
      bufferWatchDogStr, channelWrappersCoord)

    var contact = contactKernel.!
    contact = contact.remorph(wrappersStr)
    println(s"Morph composition: ${contact.myAlternative}")

    contact.printContact()

  }

}

