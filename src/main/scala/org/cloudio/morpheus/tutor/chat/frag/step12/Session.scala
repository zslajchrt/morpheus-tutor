package org.cloudio.morpheus.tutor.chat.frag.step12

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step7._

/**
* Masking alternatives to restrict degrees of freedom of a composite
*
* Created by zslajchrt on 20/05/15.
*/
object Session {


  def main(args: Array[String]) {

    val contactModel = parse[(OfflineContact or OnlineContact) with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel)](true)

    var channelCoord: Int = 0
    val channelStr = promote[StandardOutputChannel or MemoryOutputChannel](new LastRatingStrategy[contactModel.Model](), channelCoord)
    var printerCoord: Int = 0
    val printerStr = promote[ContactRawPrinter or ContactPrettyPrinter](channelStr, printerCoord)
    var statusCoord: Int = 0
    val contactStr = promote[OfflineContact or OnlineContact](printerStr, statusCoord)

    val contactData = ContactData("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)

    val contactKernel = singleton(contactModel, contactStr)

    def printAllAlts(): Unit = {
      for (i <- 0 to 1; j <- 0 to 1; k <- 0 to 1) {
        channelCoord = i
        printerCoord = j
        statusCoord = k
        contactKernel.~.remorph
        println(contactKernel.~.myAlternative)
      }
    }

    printAllAlts()

    println("----")

    var fixMemOut = 1
    val maskStrategy = mask[\?[MemoryOutputChannel]](contactStr, fixMemOut)
    contactKernel.~.remorph(maskStrategy)
    printAllAlts()

    println("----")

    var fixPretty = Set((0, 1))
    val ratingStrategy = rate[ContactPrettyPrinter](contactKernel.~.strategy, fixPretty)
    contactKernel.~.remorph(ratingStrategy)
    printAllAlts()

    println("----")

    fixMemOut = 0
    contactKernel.~.remorph
    printAllAlts()

    println("----")

    fixPretty = Set((0, 0))
    contactKernel.~.remorph
    printAllAlts()


  }
}
