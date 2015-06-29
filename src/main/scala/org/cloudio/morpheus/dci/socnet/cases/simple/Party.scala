package org.cloudio.morpheus.dci.socnet.cases.simple

import java.util.Date

import org.cloudio.morpheus.dci.socnet.objects._

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * 
 * Created by zslajchrt on 29/06/15.
 */

@fragment
trait Party {
  // This role is a projection of the Person entity.
  // This role's scene consists of faces of the contact parties 
  this: Person with PartyScene =>

  // the role's state
  private var lastMessages: List[Message] = Nil

  // just a helper used by this and its faces 
  protected def findByNick(nick: String): Option[Person with Contact] = contacts.find(_.nick == nick)
  
  // called from contact faces when another party sends a message through a face
  protected def onMessageReceived(message: Message): Unit = {
    addMessage(message)
    lastMessages ::= message
    self.onMessage(message)
  }

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

trait MessageListener {
  def onMessage(message: Message): Unit
}

trait PartyScene {

  def contacts: List[Person with Contact]

  def self: Person with MessageListener

}


// this face represents the "extension" of the user's mind into the context of the chat application
@fragment
trait SelfParty extends MessageListener {
  this: Person with Party with PartyScene =>

  def sendMessageTo(nick: String, msg: String) = {
    for (target <- findByNick(nick))
      target.message(msg, self)
  }

  def askForPhone(nick: String): Option[String] = {
    for (target <- findByNick(nick);
         phone <- traverseContactsForPhone(target, self, Nil)) yield phone
  }

  override def onMessage(message: Message): Unit = {
    println(s"Message received from ${message.from.nick}:")
    println(message.message)
  }

}


// an abstract face of the party
@dimension
trait Contact {
  def message(msg: String, from: Person): Unit

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String]
}

// the friendly face of the party
@fragment
trait Friend extends Contact {
  this: Person with Party with PartyScene =>

  override def message(msg: String, from: Person): Unit = {
    onMessageReceived(Message(msg, from, new Date()))
  }

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
  this: Party =>

  override def message(msg: String, from: Person): Unit = {
    onMessageReceived(Message(msg, from, new Date()))
  }

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = None
}

// Assembling


// the morph types
object Context {
  type PartyMorphType = Person with Party with PartyScene with (Friend or Associate or SelfParty)
  val partyMorphModel = parse[PartyMorphType](true)
}

class Context(network: List[Person]) {

  import Context._

  private val partyKernels: Map[String, partyMorphModel.Kernel] = network.map(createParty).toMap

  private def createParty(person: Person): (String, partyMorphModel.Kernel) = {
    implicit val personFrag = external(person)
    implicit val partySceneFrag = external[PartyScene](new PartySceneImpl(person))
    val partyKernel = singleton(partyMorphModel, rootStrategy(partyMorphModel))
    (person.nick, partyKernel)
  }

  private class PartySceneImpl(person: Person) extends PartyScene {
    lazy val contacts: List[Person with Contact] = for (conn <- person.connections) yield {
      partyKernels.get(conn.person.nick) match {
        case None =>
          sys.error(s"Unidentified person ${conn.person.nick}")
        case Some(contactKernel) =>
          if (conn.person.isTrusted(person))
            asMorphOf[Person with Friend](contactKernel)
          else
            asMorphOf[Person with Associate](contactKernel)
      }
    }

    lazy val self = asMorphOf[Person with MessageListener](partyKernels(person.nick))
  }

  def connect(selfPartyNick: String): Option[Person with SelfParty] = {
    for (selfParty <- partyKernels.get(selfPartyNick))
      yield asMorphOf[Person with SelfParty](selfParty)
  }

}

// Application

object App {


  def main(args: Array[String]) {

    def newPerson(id: Int) = new Someone(s"person$id", s"Pepa$id", s"Novak$id", s"pepa$id@depo.cz", s"+420 00$id", Address("Prague", s"Moskevska $id", "Czech Rep"))
    val p1 = newPerson(1)
    val p2 = newPerson(2)
    val p3 = newPerson(3)

    // P1 -> (P2, P3*) (* means 'trusted')
    // P2 -> (P3*)
    // P3 -> (P1*, P2*)

    p1.addConnection(Connection(p2, trusted = false))
    p1.addConnection(Connection(p3, trusted = true))
    p2.addConnection(Connection(p3, trusted = true))
    p3.addConnection(Connection(p1, trusted = true))
    p3.addConnection(Connection(p2, trusted = true))

    val ctx = new Context(List(p1, p2, p3))

    def connectParty(nick: String): Person with SelfParty = ctx.connect(nick) match {
      case None => sys.error(s"Unknown nick $nick")
      case Some(p) => p
    }

    val connected1 = connectParty("person1")
    connected1.sendMessageTo("person3", "Hello1")
    val connected3 = connectParty("person3")
    connected1.sendMessageTo("person3", "Hello2")
    connected3.sendMessageTo("person1", "Hello3")

    val person2phone = connected1.askForPhone("person2")
    println(s"Person 2's phone: $person2phone")


  }

}