package org.cloudio.morpheus.tutor.chat.frag.step9

import java.util.Locale

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

case class BufferWatchDogConfig_(bufferWatchDogLimit: Int) extends BufferWatchDogConfig

@fragment @wrapper
trait BufferWatchDog extends MemoryOutputChannel with BufferWatchDogConfig {

  override def printText(text: String): Unit = {
    super.printText(text)
    if (outputBuffer.size > bufferWatchDogLimit) {
      print(outputBuffer)
      outputBuffer.clear()
    }
  }

}

object Session {

  def main(args: Array[String]) {

    val contactCfg = ContactConfig_("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA)
    implicit val contactFrag = single[Contact, ContactConfig](contactCfg)
    implicit val bufWDFrag = single[BufferWatchDog, BufferWatchDogConfig](BufferWatchDogConfig_(100))
    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or (MemoryOutputChannel with \?[BufferWatchDog]))]

    var printerAltNum: Int = 0
    var channelAltNum: Int = 1
    var bufferWDAltNum: Int = 1
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelAltNum)
    val morphStrategy3 = promote[\?[BufferWatchDog]](morphStrategy2, bufferWDAltNum)

    val contact1 = contactCmp.morph(morphStrategy3)
    println(s"Morph composition: ${contact1.myAlternative}")

    for (i <- 0 to 50) {
      contact1.printContact()
    }

  }

}
