package org.cloudio.morpheus.tutor.chat.frag.step5

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact
import org.cloudio.morpheus.tutor.chat.frag.step4.{MemoryOutputChannel, StandardOutputChannel, ContactPrettyPrinter, ContactRawPrinter}

/**
 * Introducing morphing strategies and immutable morphs.
 *
 * Created by zslajchrt on 04/05/15.
 */
object Session {

  def main(args: Array[String]) {
    val contactKernel = singleton[Contact
      with (ContactRawPrinter or ContactPrettyPrinter)
      with (StandardOutputChannel or MemoryOutputChannel)]

    var contact = contactKernel.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var altNum: Int = 0
    val morphStrategy = promote[contactKernel.Model](altNum)

    println(s"There is ${morphStrategy.altsCount} alternatives")

    contact = contact.remorph(morphStrategy)
    println(contact.myAlternative)
    //contact.printContact()

    altNum = 1
    contact = contact.remorph
    println(contact.myAlternative)
    //contact.printContact()

    altNum = 2
    contact = contact.remorph
    println(contact.myAlternative)
    //contact.printContact()

    altNum = 3
    contact = contact.remorph
    println(contact.myAlternative)
    //contact.printContact()
  }

  /**
   * The switch strategy using a submodel of the morph model.
   */
  def main2(args: Array[String]) {

    val contactKernel = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactKernel.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var printerCoord: Int = 0
    var channelCoord: Int = 0
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactKernel.Model](), printerCoord)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelCoord)

    def morphContact(): Unit = {
      val morph = contactKernel.morph(morphStrategy2)
      println(morph.myAlternative)
      morph.printContact()
    }

    for (i <- 0 to 1; j <- 0 to 1) {
      printerCoord = i
      channelCoord = j
      morphContact()
    }

    // The strategy calculates the the alt index as the modulo of i and altsCount
    channelCoord = 1
    for (i <- 0 to 100) {
      printerCoord = i / 2
      channelCoord = i % 2
      morphContact()
    }

  }

}
