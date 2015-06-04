package org.cloudio.morpheus.tutor.chat.frag.step11

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
* References
*
* Created by zslajchrt on 06/05/15.
*/


object ContactKernelFactory {

  var printerCoord: Int = 0
  var channelCoord: Int = 0

  def makeContactKernel(firstName: String,
                        lastName: String,
                        male: Boolean,
                        email: String,
                        nationality: Locale): &[(OfflineContact or OnlineContact) with ContactPrinter] = {

    val contactData = ContactData(firstName, lastName, male,email, nationality)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)

    val contactModel = parse[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel)](true)

    val rootStr = rootStrategy(contactModel)
    val printerDimStr = promote[ContactRawPrinter or ContactPrettyPrinter](rootStr, printerCoord)
    val channelDimStr = promote[StandardOutputChannel or MemoryOutputChannel](printerDimStr, channelCoord)

    val contactKernel = singleton(contactModel, channelDimStr)

    contactKernel
  }

}

trait ContactStatusVisitor[T] {
  def visitOfflineContact(contact: OfflineContact): T

  def visitOnlineContact(contact: OnlineContact): T
}

class ContactStatusAcceptor(contactStatusRef: &[(OfflineContact or OnlineContact)]) {

  private val contactStatus = *(contactStatusRef).~

  def acceptVisitor[T](vis: ContactStatusVisitor[T]): T = {
    // refresh the status, it does not re-instantiate the proxy's delegate unless its composition changes
    //contactStatus.remorph()

    contactStatus.remorph() match {
      case c: OfflineContact => vis.visitOfflineContact(c)
      case c: OnlineContact => vis.visitOnlineContact(c)
      case _ => sys.error("Unexpected status")
    }
  }
}

class ContactStatusController(contactStatusRef: &[OfflineContact or OnlineContact]) {

  def setStatus(active: Boolean): Unit = {
    remorph(contactStatusRef, if (active) 1 else 0)
  }

}


@dimension @wrapper
trait FunnyOutputChannel extends OutputChannel {

  abstract override def printText(text: String): Unit = {
    super.printText(text.reverse)
  }

}

object Session {

  def useKernel(kernel: MorphKernel[(OfflineContact or OnlineContact) with
    (ContactRawPrinter or ContactPrettyPrinter) with
    (StandardOutputChannel or MemoryOutputChannel)] {
    type LUB = Contact with ContactPrinter with OutputChannel
  }): Unit = {

    val contact = kernel.~
    contact.printContact()
  }

  def main(args: Array[String]): Unit = {
    val contactData = ContactData("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)

    val kernel = singleton[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel)]

    useKernel(kernel)

    val kernelRef1: &[Contact] = kernel
    val subKernel1: MorphKernel[Contact] {
      type LUB = Contact
      type ConformLevel = TotalConformance } = *(kernelRef1)

    val contact: Contact with MutableMorphMirror[Contact] = subKernel1.~
    //val contact = subKernel.~
    println(contact.myAlternative.mkString("\n"))
    println(contact.email)

    val kernelRef2: &[OfflineContact] = kernel
    val subKernel2: MorphKernel[OfflineContact] { type LUB = OfflineContact } = *(kernelRef2)
    subKernel2.~.tryOnline()

    val kernelRef3: &[(OfflineContact or OnlineContact) with ContactPrinter] = kernel
    val subKernel3: MorphKernel[(OfflineContact or OnlineContact) with ContactPrinter] { type LUB = Contact with ContactPrinter } = *(kernelRef3)
    var contactCoord: Int = 1
    val contactDimStr = promote[(OfflineContact or OnlineContact) with ContactPrinter](subKernel3.defaultStrategy, contactCoord)
    var contact3 = subKernel3.!.remorph(contactDimStr)
    println(contact3.myAlternative.mkString("\n"))
    contact3.printContact

    val kernelRef4: &[(OfflineContact or OnlineContact) with ContactPrinter with $[FunnyOutputChannel]] = kernel
    val subKernel4: MorphKernel[(OfflineContact or OnlineContact) with ContactPrinter with FunnyOutputChannel] { type LUB = Contact with ContactPrinter with FunnyOutputChannel } = *(kernelRef4, single[FunnyOutputChannel])
    println(subKernel4.~.myAlternative.mkString("\n"))
    println(subKernel4.~.printContact)

    def tryMakeOnline(kernelRef: &[OfflineContact]): Unit = {
      val kernel = *(kernelRef)
      kernel.~.tryOnline()
    }
    tryMakeOnline(kernel)

    def createContactKernel(): &[OfflineContact] = {
      kernel
    }

    val subKernel5 = *(createContactKernel())
    subKernel5.~.tryOnline()

  }

  def main2(args: Array[String]) {

    // view, hidden fragments ...

    // The kernel factory creates a kernel with hidden dimensions
    val contactKernelRef = ContactKernelFactory.makeContactKernel("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    val contactKernel = *(contactKernelRef)
    val contact = contactKernel.~

    // Print both the visible and hidden fragments
    println(contact.myAlternative)

    // Altering the visible dimension should not affect the hidden dimensions
    var contactCoord: Int = 1
    val contactDimStr = promote[OfflineContact or OnlineContact](contactKernel.defaultStrategy, contactCoord)
    contact.remorph(contactDimStr)

    println(contact.myAlternative)
    contact.printContact

    // Controlling the hidden dimensions
    ContactKernelFactory.printerCoord = 1
    ContactKernelFactory.channelCoord = 0
    contact.remorph(contactDimStr)

    println(contact.myAlternative)
    contact.printContact

    // Creating a kernel view by means of a kernel reference
    // (OfflineContact or OnlineContact) with ContactPrinter => (OfflineContact or OnlineContact)

    val contactAcceptor = new ContactStatusAcceptor(contactKernel.~) // using contactCmp.~ instead contactCmp links the reference with the source morph via its current alternatives
    val contactVisitor = new ContactStatusVisitor[Unit] {

      override def visitOfflineContact(contact: OfflineContact): Unit = {
        println(s"${contact.lastName} is offline")
      }

      override def visitOnlineContact(contact: OnlineContact): Unit = {
        println(s"${contact.lastName} is online")
      }
    }
    contactAcceptor.acceptVisitor(contactVisitor)

    contactCoord = 1
    contactKernel.~.remorph()

    contactAcceptor.acceptVisitor(contactVisitor)

    contactCoord = 0
    contactKernel.~.remorph()

    contactAcceptor.acceptVisitor(contactVisitor)

    contactCoord = 1
    contactKernel.~.remorph()

    contactAcceptor.acceptVisitor(contactVisitor)

    // Controller

    // Using a sub-kernel to reshape the owning morph

    val controller: ContactStatusController = new ContactStatusController(contactKernel)
    controller.setStatus(false)

    contactAcceptor.acceptVisitor(contactVisitor)

    controller.setStatus(true)

    contactAcceptor.acceptVisitor(contactVisitor)
  }

}
