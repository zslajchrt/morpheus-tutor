package org.cloudio.morpheus.dci.socnet.cases.phoneRequest

import org.cloudio.morpheus.dci.socnet.objects._
import org.cloudio.morpheus.dci.socnet.objects.Person._

import org.morpheus._
import org.morpheus.Morpheus._


/**
 *
 * Created by zslajchrt on 29/06/15.
 */


trait Scene {

  def contacts: List[PersonPublic with PersonFace]

}

trait PartyScene extends Scene {

}

@fragment
protected trait PartyRole {
  // This role is a projection of the Person entity.
  // This role's scene consists of faces of the contact parties
  this: PersonPublic with Scene =>

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

trait RequesterScene extends Scene {

  def client: Client

}

// The end user is identified with this role. It corresponds to the UI interface of the end user.
// The operations - i.e. askForPhone only - and the scene will be reflected on the end user's terminal.
@fragment
trait Requester {
  this: PersonPublic with PersonPrivate with PartyRole with RequesterScene =>

  def askForPhone(phoneOwnerNick: String): Option[String] = {
    for (target <- findByNick(phoneOwnerNick);
         phone <- traverseContactsForPhone(target, this /* this requester IS a PersonBasic */, Nil)) yield phone
  }

  private def findByNick(nick: String): Option[PersonPublic with PersonFace] = contacts.find(_.nick == nick)

  def start(): Unit = {
    client.receive({
      case "" =>
        val requestedPhone = askForPhone("")
        client.send(Map("requestedPhone" -> requestedPhone))
    })
    client.send(Map("nick" -> nick, "phone" -> phone))
  }

}

// an abstract face of the party
@dimension
trait PersonFace {
  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String]
}

// the friendly face of the party
@fragment
trait Friend extends PersonFace {
  this: PersonPublic with PersonPrivate with PartyRole with PartyScene =>

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
  this: PersonConnections with PartyRole =>

  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (isTrusted(path.head.nick))
      traverseContactsForPhone(target, onBehalfOf, path)
    else
      None
  }
}

// Assembling


// The Party role's valid forms expressed by means of its morph type
object Party {
  type PartyMorphType = PersonPublic with PersonConnections with
    (($[Friend] with PersonPrivate with $[PartyScene]) or
      ($[Associate] with $[PartyScene]) or
      ($[Requester] with PersonPrivate with $[RequesterScene] with $[ClientMock])) with
    $[PartyRole]
//  type PartyMorphType2 = PersonPublic with PersonConnections with
//    ((Friend with PersonPrivate with PartyScene) or // only those in the friend relationship reveal their private data
//      (Associate with PartyScene) or
//      (Requester with PersonPrivate with RequesterScene with ClientMock)) with
//    PartyRole // every role is equipped with this toolbox
  val partyMorphModel = parse[PartyMorphType](true, true)
}


class PhoneRequest(network: List[personMorphModel.Kernel]) {

  import Party._

  private val partyKernels: Map[String, partyMorphModel.Kernel] = network.map(createParty).toMap

  private def createParty(personKernel: personMorphModel.Kernel): (String, partyMorphModel.Kernel) = {
    //val personKernelRef: &[PersonPublic with PersonConnections with (($[Friend] with PersonPrivate with $[PartyScene]) or ($[Associate] with $[PartyScene]) or ($[Requester] with PersonPrivate with $[RequesterScene] with $[ClientMock])) with $[PartyRole]] = personKernel.~ // using ~ ensures that the subjective morph is connected with the original morph
    val personKernelRef: &[PartyMorphType] = personKernel.~ // using ~ ensures that the subjective morph is connected with the original morph
    val personWithCons = asMorphOf[PersonPublic with PersonConnections](personKernel)
    val partyKernel: partyMorphModel.Kernel = *(personKernelRef,
      single[PartyRole],
      single[Friend],
      single[Associate],
      single[Requester],
      single[ClientMock],
      external[RequesterScene](new RequesterSceneImpl(personWithCons)),
      external[PartyScene](new PartySceneImpl(personWithCons))
    )
    (personKernel.~.nick, partyKernel)
  }

  private abstract class SceneImpl(person: PersonPublic with PersonConnections) extends Scene {

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

  private class PartySceneImpl(person: PersonPublic with PersonConnections) extends SceneImpl(person) with PartyScene {
  }

  private class RequesterSceneImpl(person: PersonPublic with PersonConnections) extends SceneImpl(person) with RequesterScene {
    lazy val client: Client = null
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