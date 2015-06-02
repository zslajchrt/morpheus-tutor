package org.cloudio.morpheus.tutor.chat.frag.step8

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
 * Dimension wrappers
 *
 * Created by zslajchrt on 05/05/15.
 */


@dimension
@wrapper
trait ChannelMonitor extends OutputChannel {

  private var totalChars_ = 0

  def totalChars = totalChars_

  abstract override def printText(text: String): Unit = {
    super.printText(text)
    totalChars_ += text.length
  }
}

@dimension
@wrapper
trait NewLineAppender extends OutputChannel {
  abstract override def printText(text: String): Unit = {
    super.printText(text + "\n")
  }
}


object Session {

  def main(args: Array[String]) {

    val contactData = ContactData("Pepa", "Novák", male = true, email = "pepa@depo.cz", Locale.CANADA)

    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)

    val contactKernel = singleton[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel) with
      \?[NewLineAppender] with
      \?[ChannelMonitor]]

    var contactAltNum: Int = 1
    val contactStr = promote[OfflineContact or OnlineContact](
      RootStrategy[contactKernel.Model](), contactAltNum)

    var printerAltNum: Int = 1
    val printerStr = promote[ContactRawPrinter or ContactPrettyPrinter](
      contactStr, printerAltNum)

    var channelAltNum: Int = 0
    val channelStr = promote[StandardOutputChannel or MemoryOutputChannel](
      printerStr, channelAltNum)

    var channelWrappersAltNum: Int = 3
    val wrappersStr = promote[\?[ChannelMonitor] with \?[NewLineAppender]](
      channelStr, channelWrappersAltNum)

    var contact = contactKernel.!
    contact = contact.remorph(wrappersStr)
    println(s"Morph composition: ${contact.myAlternative}")
    contact.printContact()

    select[ChannelMonitor](contact) match {
      case None =>
      case Some(chMon) => println(s"Characters written: ${chMon.totalChars}")
    }

  }
}

