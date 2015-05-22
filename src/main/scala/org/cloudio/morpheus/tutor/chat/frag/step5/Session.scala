package org.cloudio.morpheus.tutor.chat.frag.step5

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact
import org.cloudio.morpheus.tutor.chat.frag.step4.{MemoryOutputChannel, StandardOutputChannel, ContactPrettyPrinter, ContactRawPrinter}

/**
 * Introducing morpher strategies and immutable morphs.
 *
 * Created by zslajchrt on 04/05/15.
 */
object Session {

  def main1(args: Array[String]) {

    val contactCmp = singleton[Contact
      with (ContactRawPrinter or ContactPrettyPrinter)
      with (StandardOutputChannel or MemoryOutputChannel)]

    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var altNum: Int = 0
    val morphStrategy = promote[contactCmp.Model](altNum)
    println(s"There is ${morphStrategy.altsCount} alternatives")

    val morph1 = contactCmp.morph(morphStrategy)
    morph1.printContact()

    altNum = 1
    val morph2 = contactCmp.morph(morphStrategy)
    morph2.printContact()

  }

  /**
   * The switch strategy using a submodel of the composite model.
   */
  def main(args: Array[String]) {

    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var printerAltNum: Int = 0
    var channelAltNum: Int = 0
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerAltNum)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelAltNum)

    def morphContact(): Unit = {
      val morph = contactCmp.morph(morphStrategy2)
      println(morph.myAlternative)
      morph.printContact()
    }

    for (i <- 0 to 1; j <- 0 to 1) {
      printerAltNum = i
      channelAltNum = j
      morphContact()
    }

    // The strategy calculates the the alt index as the modulo of i and altsCount
    channelAltNum = 1
    for (i <- 0 to 100) {
      printerAltNum = i / 2
      channelAltNum = i % 2
      morphContact()
    }

  }

}
