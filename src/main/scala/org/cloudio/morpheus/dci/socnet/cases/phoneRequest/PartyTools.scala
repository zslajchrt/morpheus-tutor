package org.cloudio.morpheus.dci.socnet.cases.phoneRequest

import org.cloudio.morpheus.dci.socnet.objects._
import org.cloudio.morpheus.dci.socnet.objects.Person._

import org.morpheus._
import org.morpheus.Morpheus._


/**
*
* Created by zslajchrt on 29/06/15.
*/

trait PartyScene {

  def contacts: List[PersonPublic with PersonFace]

}

@fragment
trait Requester {
  this: PersonPublic with PartyScene with PartyTools =>

  def askForPhone(phoneOwnerNick: String): Option[String] = {
    for (target <- findByNick(phoneOwnerNick);
         phone <- traverseContactsForPhone(target, this /* this requester IS a PersonBasic */, Nil)) yield phone
  }

  // just a helper used by this and its faces
  private def findByNick(nick: String): Option[PersonPublic with PersonFace] = contacts.find(_.nick == nick)

}

// an abstract face of the party
@dimension
trait PersonFace {
  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String]
}

// the friendly face of the party
@fragment
trait Friend extends PersonFace {
  this: PersonPublic with PersonPrivate with PartyTools with PartyScene =>

  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (target.nick == this.nick) {
      if (examineRequester(onBehalfOf)) {
        Some(phone)
      } else {
        None
      }
    } else {
      traverseContactsForPhone(target, onBehalfOf, path)
    }
  }

  private def examineRequester(requester: PersonPublic): Boolean = {
    true
  }

}

// just an associate
@fragment
trait Associate extends PersonFace {
  this: PersonConnections with PartyTools =>

  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (isTrusted(path.head.nick))
      traverseContactsForPhone(target, onBehalfOf, path)
    else
      None
  }
}

@fragment
protected trait PartyTools {
  // This role is a projection of the Person entity.
  // This role's scene consists of faces of the contact parties
  this: PersonPublic with PartyScene =>

  // a helper for the traversal of the graph of contacts
  protected def traverseContactsForPhone(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (path.exists(_.nick == this.nick)) {
      None
    } else {
      var maybePhone: Option[String] = None
      val contactIterator = contacts.iterator
      while (contactIterator.hasNext && !maybePhone.isDefined) {
        maybePhone = contactIterator.next().phoneRequest(target, onBehalfOf, this :: path)
      }
      maybePhone
    }

  }

}

// Assembling


// The Party role's valid forms expressed by means of its morph type
object Party {
  type PartyMorphType = PersonPublic with PersonConnections with
  ((Friend with PersonPrivate) or // only those in the friend relationship reveal their private data
      Associate or
      Requester) with
    PartyScene with // every role has its scene
    PartyTools // every role is equipped with this toolbox
  val partyMorphModel = parse[PartyMorphType](true)
}

class PhoneRequest(network: List[personMorphModel.Kernel]) {

  import Party._

  private val partyKernels: Map[String, partyMorphModel.Kernel] = network.map(createParty).toMap

  private def createParty(personKernel: personMorphModel.Kernel): (String, partyMorphModel.Kernel) = {
    val personKernelRef: &[PersonPublic with PersonConnections with (($[Friend] with PersonPrivate) or $[Associate] or $[Requester]) with $[PartyScene] with $[PartyTools]] = personKernel.~ // using ~ ensures that the subjective morph is connected with the original morph
    val personWithCons = asMorphOf[PersonPublic with PersonConnections](personKernel)
    val partyKernel: partyMorphModel.Kernel = *(personKernelRef,
        single[PartyTools],
        external[PartyScene](new PartySceneImpl(personWithCons)),
        single[Friend],
        single[Associate],
        single[Requester])
    (personKernel.~.nick, partyKernel)
  }

  private class PartySceneImpl(person: PersonPublic with PersonConnections) extends PartyScene {
    lazy val contacts: List[PersonPublic with PersonFace] = for (conn <- person.connections) yield {
      partyKernels.get(conn.person.nick) match {
        case None =>
          sys.error(s"Unidentified person ${conn.person.nick}")
        case Some(contactKernel) =>
          if (conn.person.isTrusted(person.nick))
            asMorphOf[PersonPublic with Friend](contactKernel)
          else
            asMorphOf[PersonPublic with Associate](contactKernel)
      }
    }
  }

  def apply(requesterNick: String, phoneOwnerNick: String) = {
    partyKernels.get(requesterNick) match {
      case None => sys.error(s"No party with nick $requesterNick found")
      case Some(requesterKernel) =>
        val requester = asMorphOf[PersonPublic with Requester](requesterKernel)
        requester.askForPhone(phoneOwnerNick)
    }
  }

}

// Application

object App {

  def main(args: Array[String]) {

    def newPerson(id: Int): personMorphModel.Kernel = {
      implicit val personFrag = single[SomeonePublic, PersonPublic](PersonPublicInit(s"person$id", s"Pepa$id", s"Novak$id", s"pepa$id@depo.cz"))
      implicit val personPrivateFrag = single[SomeonePrivate, PersonPrivate](PersonPrivateInit(s"+420 00$id", Address("Prague", s"Moskevska $id", "Czech Rep")))
      singleton(personMorphModel, rootStrategy(personMorphModel))
    }


    val p1 = newPerson(1)
    val p2 = newPerson(2)
    val p3 = newPerson(3)

//    P1 -> (P2, P3*) (* means 'trusted')
//    P2 -> (P3*)
//    P3 -> (P1*, P2*)

    implicit def personKernelToPersonWithContacts(personKernel: personMorphModel.Kernel): PersonPublic with PersonConnections = {
      asMorphOf[PersonPublic with PersonConnections](personKernel)
    }

    p1.addConnection(Connection(p2, trusted = false))
    p1.addConnection(Connection(p3, trusted = true))
    p2.addConnection(Connection(p3, trusted = true))
    p3.addConnection(Connection(p1, trusted = true))
    p3.addConnection(Connection(p2, trusted = true))

    val phoneRequest = new PhoneRequest(List(p1, p2, p3))

    val person2phone = phoneRequest("person1", "person2")
    println(s"Person 2's phone: $person2phone")

  }

}