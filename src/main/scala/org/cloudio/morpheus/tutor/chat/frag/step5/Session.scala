package org.cloudio.morpheus.tutor.chat.frag.step5

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact
import org.cloudio.morpheus.tutor.chat.frag.step4.{MemoryOutputChannel, StandardOutputChannel, ContactPrettyPrinter, ContactRawPrinter}

@dimension
trait Color {

}

@fragment
trait Red extends Color {
}

@fragment
trait Yellow extends Color {
}

@fragment
trait Green extends Color {
}

/**
 * Introducing morpher strategies and immutable morphs.
 *
 * Created by zslajchrt on 04/05/15.
 */
object Session {

  def main1(args: Array[String]) {
    val tlModel = parse[Red or Yellow or Green](true)

    var lightSel = Set((0, 1), (1, 0), (2, 0))
    val tlStrategy = rate[Red or Yellow or Green](lightSel)
    val tlComp = compose(tlModel, tlStrategy)

    var tl = tlComp.!


    lightSel = Set((0, 0), (1, 1), (2, 0))
    tl = tl.remorph

//    val contactModel = parse[Contact
//      with (ContactRawPrinter or ContactPrettyPrinter)
//      with (StandardOutputChannel or MemoryOutputChannel)](true)
//    var altNum: Int = 0
//    //val morphStrategy = promote[contactModel.Model](altNum)
//    var printerSel = Set((0, 1), (1, 0))
//    val morphStrategy = rate[ContactRawPrinter or ContactPrettyPrinter](rootStrategy(contactModel), printerSel)
//
//    val contactKernel = singleton(contactModel, morphStrategy)
//
//    var contact = contactKernel.!
//
//    contact.firstName = "Pepa"
//    contact.lastName = "Novák"
//    contact.male = true
//    contact.nationality = Locale.CANADA
//
//    println(s"There is ${morphStrategy.altsCount} alternatives")
//
//    //contact.remorph(morphStrategy)
//    contact = contact.remorph
//    contact.printContact()
//
//    altNum = 1
//    contact = contact.remorph
//    contact.printContact()
//
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

    var printerCoord: Int = 0
    var channelCoord: Int = 0
    val morphStrategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](RootStrategy[contactCmp.Model](), printerCoord)
    val morphStrategy2 = promote[StandardOutputChannel or MemoryOutputChannel](morphStrategy1, channelCoord)

    def morphContact(): Unit = {
      val morph = contactCmp.morph(morphStrategy2)
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
