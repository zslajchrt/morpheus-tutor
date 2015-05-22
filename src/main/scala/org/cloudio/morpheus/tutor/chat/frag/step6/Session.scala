package org.cloudio.morpheus.tutor.chat.frag.step6

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact
import org.cloudio.morpheus.tutor.chat.frag.step4.{MemoryOutputChannel, StandardOutputChannel, ContactPrettyPrinter, ContactRawPrinter}

/**
 * Mutable morphs.
 *
 * Created by zslajchrt on 04/05/15.
 */

@fragment
trait PrinterControl {
  this: ContactRawPrinter or ContactPrettyPrinter =>


  def rawPrint(): Unit = {
    remorph(this, 0)
  }

  def prettyPrint(): Unit = {
    remorph(this, 1)
  }
}

@fragment
trait OutputControl {
  this: StandardOutputChannel or MemoryOutputChannel =>


  def useStandardOutput(): Unit = {
    remorph(this, 0)
  }

  def useMemoryOutput(): Unit = {
    remorph(this, 1)
  }
}

object Session {

  /**
   * Explicit morpher notification via `notifyMorpher`
   */
  def main1(args: Array[String]) {

    val contactCmp = singleton[Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var altNum: Int = 0
    val morphStrategy = promote[contactCmp.Model](altNum)

    val morph = contactCmp.morph_~(morphStrategy)
    morph.printContact()

    altNum = 3
    morph.notifyMorpher()

    select[MemoryOutputChannel](morph) match {
      case None =>
        morph.printContact()
      case Some(memChannel) =>
        morph.printContact()
        println(memChannel.outputBuffer)
    }
  }

  /**
   * Indirect morpher notification via an event
   */
  def main2(args: Array[String]) {

    implicit val mutFrag = mutableFragment()
    val contactCmp = singleton[MutableFragment with Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    var altNum: Int = 0
    val morphStrategy = promote[contactCmp.Model](altNum)

    val morph = contactCmp.morph_~(morphStrategy)
    morph.startListening()

    morph.fire("notify", null, null)
    morph.printContact()

    altNum = 1
    morph.fire("notify", null, null)
    morph.printContact()

    morph.stopListening()
  }

  /**
   * Indirect morpher notification through the event monitor
   */
  def main3(args: Array[String]) {
    val followUpEvent = CompositeEvent("morph", null, null)
    val monitor = EventMonitor[Int]("altNum", followUpEvent)

    implicit val mutFrag = mutableFragment(monitor)
    val contactCmp = singleton[MutableFragment with Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactCmp.!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    val morphStrategy = promote[contactCmp.Model](monitor.status())

    val morph = contactCmp.morph_~(morphStrategy)
    morph.startListening(followUpEvent.nameSelector)

    val setAltEvent = monitor.makeEvent(0)
    morph.fireEvent(setAltEvent)
    morph.printContact()

    morph.fireEvent(setAltEvent.copy(eventValue = 1))
    morph.printContact()

    morph.stopListening()
  }

  /**
   * Indirect morpher notification through a fragment member
   */
  def main(args: Array[String]): Unit = {


    val contactCmp = singleton[PrinterControl with OutputControl with Contact with (ContactRawPrinter or ContactPrettyPrinter) with (StandardOutputChannel or MemoryOutputChannel)]
    val contact = contactCmp.~

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.useMemoryOutput()
    contact.printContact()
    contact.rawPrint()
    contact.printContact()
    contact.prettyPrint()
    contact.printContact()

    contact.useStandardOutput()
    contact.printContact()
    contact.rawPrint()
    contact.printContact()
    contact.prettyPrint()
    contact.printContact()

    // pass the contact with the control interface to a Java method
    ContactClient.useContact(contact, true)
    ContactClient.useContact(contact, false)
    ContactClient.useContact(contact, true)
  }
}
