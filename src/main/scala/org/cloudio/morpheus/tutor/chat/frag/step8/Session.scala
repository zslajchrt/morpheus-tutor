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

@dimension @wrapper
trait ChannelMonitor extends OutputChannel {

  private var totalChars_ = 0
  def totalChars = totalChars_

  abstract override def printText(text: String): Unit = {
    super.printText(text)
    totalChars_ += text.length
  }
}

@dimension @wrapper
trait NewLineAppender extends OutputChannel {
  abstract override def printText(text: String): Unit = {
    super.printText(text + "\n")
  }
}

object Session {

  def main(args: Array[String]) {

    val contactCfg = ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    implicit val contactFrag = single[Contact, ContactConfig](contactCfg)
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel) with \?[ChannelMonitor] with \?[NewLineAppender]]

    var printerAltNum: Int = 0
    var channelAltNum: Int = 0
    var channelWrappersAltNum: Int = 3
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelAltNum)
    val morphStrategy3 = promote[\?[ChannelMonitor] with \?[NewLineAppender]](morphStrategy2, channelWrappersAltNum)

    val contact1 = contactCmp.morph(morphStrategy3)
    println(s"Morph composition: ${contact1.myAlternative}")

    contact1.printContact()
    select[ChannelMonitor](contact1) match {
      case None =>
      case Some(chMon) => println(s"Characters written: ${chMon.totalChars}")
    }

  }

}
