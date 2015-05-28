package org.cloudio.morpheus.tutor.chat.frag.step3

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step1.Contact

/**
 * Abstract the printing capability.
 *
 * The illustration of the problem of the reusing of the same object with
 *
 * Created by zslajchrt on 04/05/15.
 */

@dimension
trait ContactPrinter {
  def printContact(): Unit
}

@fragment
trait ContactRawPrinter extends ContactPrinter {
  this: Contact =>

  def printContact(): Unit = {
    println(s"$firstName $lastName $nationality $male")
  }
}

@fragment
trait ContactPrettyPrinter extends ContactPrinter {
  this: Contact =>

  def printContact(): Unit = {
    println(
      s"""
         First Name: $firstName
         Last Name: $lastName
         Male: $male
         Nationality: $nationality
      """)
  }
}

@dimension
trait ContactSerializer {
  def serializeContact: String
}

@fragment
trait JsonContactSerializer extends ContactSerializer {
  this: Contact =>

  def serializeContact: String = {
    s"""
      {
         'firstName': '$firstName',
         'lastName': '$lastName',
         'male': $male,
         'nationality': '$nationality',
      }
    """
  }
}

object Session {

  def main1(args: Array[String]) {

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactScala = if (prettyPrint) {
      new Contact with ContactPrettyPrinter
    } else {
      new Contact with ContactRawPrinter
    }

    val contact = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter].!
    else
      singleton[Contact with ContactRawPrinter].!

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    // Reusing the contact fragment

    val contactFragment = contact.kernel.fragmentHolder[Contact] match {
      case None => sys.error("")
      case Some(holder) => holder.proxy
    }
    implicit val contactFragInst = external(contactFragment)

    val contactSerial = singleton[Contact with JsonContactSerializer].!
    val contactJson = contactSerial.serializeContact
    println(contactJson)


    //res5: org.morpheus.MorphKernel[_52]{type ConformLevel = contact.ConfLev} forSome { type _52 >: org.cloudio.morpheus.tutor.chat.frag.step1.Contact with org.cloudio.morpheus.tutor.chat.frag.step3.ContactPrettyPrinter with org.cloudio.morpheus.tutor.chat.frag.step1.Contact with org.cloudio.morpheus.tutor.chat.frag.step3.ContactRawPrinter <: org.cloudio.morpheus.tutor.chat.frag.step1.Contact with org.cloudio.morpheus.tutor.chat.frag.step3.ContactPrinter } = $anon$1@333e82c

    //tt.typeSymbol.info.asInstanceOf[ru.RefinedType].parents.head.typeArgs

//    org.morpheus.MorphMirror[
//      _ >: Contact with ContactPrettyPrinter with or[Ext1, Ext2] with Contact with ContactRawPrinter with or[Ext1, Ext2]
//        <: or[Ext1,Ext2] with ContactPrinter with Contact]

//    MorphMirror[_
//      >: Contact with ContactPrettyPrinter with Contact with ContactRawPrinter
//      <: ContactPrinter with Contact]

//    MorphMirror[_
//      >: Contact with ContactPrettyPrinter with or[ExtA1,ExtA2] with or[ExtB1,ExtB2] with Contact with ContactRawPrinter with or[ExtA1,ExtA2] with or[ExtB1,ExtB2] ...

//tt.typeSymbol.info.asInstanceOf[ru.RefinedType].parents.head.asInstanceOf[scala.reflect.internal.Types#ExistentialType].quantified.head.existentialBound.bounds.lo

    // Passing the contact to a Java method is possible. The method requires a 'Contact with ContactPrinter' instance as the argument.
    // The 'Pimp My Library' pattern would not work since it is a simple wrapper spiced by an implicit conversion.
    ContactClient.useContact(contact)

    // The reuse problem:
    // If we want to print the contact by the other printer we have to create a new instance of the contact.
    // There is no simple way to reuse the existing entity and couple it with another traits.
    // Note: The "pimp my library" pattern could help, however, it is limited to Scala and it is just a syntax trick.



  }

  def main2(args: Array[String]) {

    // The reuse problem solution via re-composition

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactRef: &[Contact with ContactPrinter] = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter]
    else
      singleton[Contact with ContactRawPrinter]

    val contactCmp = *(contactRef)
    val contact = contactCmp.!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    implicit val clientFrag = expose[Contact](contactCmp)
    val contactSerial = singleton[Contact with JsonContactSerializer].!
    val contactJson = contactSerial.serializeContact
    println(contactJson)
  }

  def main(args: Array[String]) {

    // The reuse problem solution via composite references:

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactKernel = singleton[Contact with (ContactPrettyPrinter or ContactRawPrinter)]
    val contact = contactKernel.!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    val contactSerialRef: &[Contact with $[ContactSerializer]] = contactKernel
    val contactSerialKernel = *(contactSerialRef, single[JsonContactSerializer])

    val contactJson = contactSerialKernel.!.serializeContact
    println(contactJson)

  }

  def main4(args: Array[String]) {

    // The reuse problem solution via composite references:

    val prettyPrint = args match {
      case Array("prettyPrint") => true
      case _ => false
    }

    val contactRef: &[Contact with ContactPrinter] = if (prettyPrint)
      singleton[Contact with ContactPrettyPrinter]
    else
      singleton[Contact with ContactRawPrinter]

    val contactCmp = *(contactRef)
    val contact = contactCmp.!
    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    contact.printContact()

    val contact2Ref: &[Contact with $[ContactPrinter]] = contactCmp
//    val printFrag = if (prettyPrint)
//      singleAsDim[ContactRawPrinter]
//    else
//      singleAsDim[ContactPrettyPrinter]
    val contact2Cmp = if (prettyPrint)
      *(contact2Ref, single[ContactRawPrinter])
    else
      *(contact2Ref, single[ContactPrettyPrinter])

    contact2Cmp.!.printContact()

  }

  /**
   * Using the Pimp My Library pattern for a comparison.
   */
  def main5(args: Array[String]) {

    object StdPr {
      implicit class StandardPrinterCaster(c: Contact) extends ContactPrinter {
        override def printContact(): Unit = {
          import c._
          print(s"$firstName $lastName $nationality $male")
        }
      }
    }

    object MemPr {
      implicit class StandardPrinterCaster(c: Contact) extends ContactPrinter {
        override def printContact(): Unit = {
          import c._
          print(s"$firstName $lastName $nationality $male")
        }
      }
    }

    val prettyPrint = args.contains("prettyPrint")

    val contact = new Contact {}

    contact.firstName = "Pepa"
    contact.lastName = "Novák"
    contact.male = true
    contact.nationality = Locale.CANADA

    if (prettyPrint) {
      import StdPr._
      contact.printContact()
    } else {
      import MemPr._
      contact.printContact()
    }

    //ContactClient.useContact(contact) // Does not compile
  }


}
