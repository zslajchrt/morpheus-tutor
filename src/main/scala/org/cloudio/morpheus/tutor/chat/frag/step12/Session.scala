package org.cloudio.morpheus.tutor.chat.frag.step12

import java.util.Locale

import org.morpheus._
import Morpheus._
import org.cloudio.morpheus.tutor.chat.frag.step11.{Online, Offline}
import org.cloudio.morpheus.tutor.chat.frag.step8._

/**
 * Rating alternatives to restrict degrees of freedom of a composite
 *
 * Created by zslajchrt on 20/05/15.
 */
object Session {


  def main(args: Array[String]) {

    implicit val contactFrg = single[Contact, ContactData](ContactConfig("Pepa", "Novák", male = true, email = "pepa@gmail.com", Locale.CANADA))
    val contactModel = parse[Contact with
      (ContactRawPrinter or ContactPrettyPrinter) with
      (StandardOutputChannel or MemoryOutputChannel) with
      (Offline or Online)](true)

    var printerAltNum: Int = 0
    var channelAltNum: Int = 0
    var statusAltNum: Int = 0

    val strategy1 = promote[ContactRawPrinter or ContactPrettyPrinter](new LastRatingStrategy[contactModel.Model](), printerAltNum)
    val strategy2 = promote[StandardOutputChannel or MemoryOutputChannel](strategy1, channelAltNum)
    val strategy3 = promote[Offline or Online](strategy2, statusAltNum)
    val contactCmp = singleton(contactModel, strategy3)

    def goThroughAllAlts(): Unit = {
      for (i <- 0 to 1; j <- 0 to 1; k <- 0 to 1) {
        printerAltNum = i
        channelAltNum = j
        statusAltNum = k
        contactCmp.~.remorph
        println(contactCmp.~.myAlternative)
      }
    }

    //goThroughAllAlts()

    var fixMemOut = Set((0, 1))
    val memoryOutputOnlyStrategy = rate[/?[MemoryOutputChannel]](strategy3, fixMemOut)

    contactCmp.~.remorph(memoryOutputOnlyStrategy)
    goThroughAllAlts()

    println("----")

    fixMemOut = Set((0, 0))
    contactCmp.~.remorph(memoryOutputOnlyStrategy)
    goThroughAllAlts()

  }
}
