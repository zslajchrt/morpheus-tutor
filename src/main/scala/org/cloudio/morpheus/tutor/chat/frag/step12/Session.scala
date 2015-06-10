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

    var printerCoord: Int = 0
    val printerStr = promote[ContactRawPrinter or ContactPrettyPrinter](new LastRatingStrategy[contactModel.Model](), printerCoord)
    var channelCoord: Int = 0
    val channelStr = promote[StandardOutputChannel or MemoryOutputChannel](printerStr, channelCoord)
    var statusCoord: Int = 0
    val contactStr = promote[OfflineContact or OnlineContact](channelStr, statusCoord)
    var fixMemOut = 0
    val maskStrategy = mask[\?[MemoryOutputChannel]](contactStr, fixMemOut)

    val contactData = ContactData("Pepa", "Novák", male = true, email="pepa@depo.cz", Locale.CANADA)
    implicit val offlineContactFrag = single[OfflineContact, Contact](contactData)
    implicit val onlineContactFrag = single[OnlineContact, Contact](contactData)

    val contactKernel = singleton(contactModel, maskStrategy)

    def printAllAlts(): Unit = {
      for (i <- 0 to 1; j <- 0 to 1; k <- 0 to 1) {
        printerCoord = i
        channelCoord = j
        statusCoord = k
        contactKernel.~.remorph
        println(contactKernel.~.myAlternative)
      }
    }

    printAllAlts()

    fixMemOut = 1
    contactKernel.~.remorph
    printAllAlts()

    println("----")

    fixMemOut = 0
    contactKernel.~.remorph
    printAllAlts()

  }
}
