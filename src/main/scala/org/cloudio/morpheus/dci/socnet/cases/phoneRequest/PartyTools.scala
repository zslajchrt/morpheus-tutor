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

  def contacts: List[Person with Contact]

  def self: Person

}

@fragment
trait Requester {
  this: Person with PartyScene with PartyTools =>

  def askForPhone(phoneOwnerNick: String): Option[String] = {
    for (target <- findByNick(phoneOwnerNick);
         phone <- traverseContactsForPhone(target, self, Nil)) yield phone
  }

  // just a helper used by this and its faces
  private def findByNick(nick: String): Option[Person with Contact] = contacts.find(_.nick == nick)

}

// an abstract face of the party
@dimension
trait Contact {
  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String]
}

// the friendly face of the party
@fragment
trait Friend extends Contact {
  this: Person with PersonPrivate with PartyTools with PartyScene =>

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = {
    if (target.nick == self.nick) {
      if (examineRequester(onBehalfOf)) {
        Some(phone)
      } else {
        None
      }
    } else {
      traverseContactsForPhone(target, onBehalfOf, path)
    }
  }

  private def examineRequester(requester: Person): Boolean = {
    true
  }

}


// just an associate, which needn't have to have the party in its contacts
@fragment
trait Associate extends Contact {
  this: PartyTools =>

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = {
    None // todo: it could return the phone under certain circumstances
  }
}

@fragment
protected trait PartyTools {
  // This role is a projection of the Person entity.
  // This role's scene consists of faces of the contact parties
  this: Person with PartyScene =>

  // a helper for the traversal of the graph of contacts
  protected def traverseContactsForPhone(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = {
    if (path.exists(_.nick == self.nick)) {
      None
    } else {
      var maybePhone: Option[String] = None
      val contactIterator = contacts.iterator
      while (contactIterator.hasNext && !maybePhone.isDefined) {
        maybePhone = contactIterator.next().phoneRequest(target, onBehalfOf, self :: path)
      }
      maybePhone
    }

  }

}

// Assembling


// the morph types
object PhoneRequest {
  type PartyMorphType = Person with PartyTools with PartyScene with ((Friend with PersonPrivate) or Associate or Requester)
  val partyMorphModel = parse[PartyMorphType](true)
}

class PhoneRequest(network: List[personMorphModel.Kernel]) {

  import PhoneRequest._

  private val partyKernels: Map[String, partyMorphModel.Kernel] = network.map(createParty).toMap

  private def createParty(personKernel: personMorphModel.Kernel): (String, partyMorphModel.Kernel) = {
    val personKernelRef: &[Person with $[PartyTools] with $[PartyScene] with (($[Friend] with PersonPrivate) or $[Associate] or $[Requester])] = personKernel
    val partyKernel: partyMorphModel.Kernel = *(personKernelRef, single[PartyTools],  external[PartyScene](new PartySceneImpl(personKernel.!)), single[Friend], single[Associate], single[Requester])
    (personKernel.!.nick, partyKernel)
  }

  private class PartySceneImpl(person: Person with PersonConnections) extends PartyScene {
    lazy val contacts: List[Person with Contact] = for (conn <- person.connections) yield {
      partyKernels.get(conn.person.nick) match {
        case None =>
          sys.error(s"Unidentified person ${conn.person.nick}")
        case Some(contactKernel) =>
          if (conn.person.isTrusted(person.nick))
            asMorphOf[Person with Friend](contactKernel)
          else
            asMorphOf[Person with Associate](contactKernel)
      }
    }

    lazy val self = asMorphOf[Person](partyKernels(person.nick))

  }


  def apply(requesterNick: String, phoneOwnerNick: String) = {
    val requester = asMorphOf[Person with Requester](partyKernels(requesterNick))
    requester.askForPhone(phoneOwnerNick)
  }

}

// Application


object App {

  def main(args: Array[String]) {

    def newPerson(id: Int): personMorphModel.Kernel = {
      implicit val personFrag = single[Someone, Person](PersonInit(s"person$id", s"Pepa$id", s"Novak$id", s"pepa$id@depo.cz"))
      implicit val personPrivateFrag = single[SomeonePrivate, PersonPrivate](PersonPrivateInit(s"+420 00$id", Address("Prague", s"Moskevska $id", "Czech Rep")))
      singleton(personMorphModel, rootStrategy(personMorphModel))
    }


    val p1 = newPerson(1)
    val p2 = newPerson(2)
    val p3 = newPerson(3)

    // P1 -> (P2, P3*) (* means 'trusted')
    // P2 -> (P3*)
    // P3 -> (P1*, P2*)

    p1.!.addConnection(Connection(p2.!, trusted = false))
    p1.!.addConnection(Connection(p3.!, trusted = true))
    p2.!.addConnection(Connection(p3.!, trusted = true))
    p3.!.addConnection(Connection(p1.!, trusted = true))
    p3.!.addConnection(Connection(p2.!, trusted = true))

    val phoneRequest = new PhoneRequest(List(p1, p2, p3))

    val person2phone = phoneRequest("person1", "person2")
    println(s"Person 2's phone: $person2phone")

  }

}