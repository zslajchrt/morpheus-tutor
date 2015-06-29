package org.cloudio.morpheus.dci.socnet.cases

import java.util.Date

import org.cloudio.morpheus.dci.socnet.objects._
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/06/15.
 */

@fragment
trait Party {
  this: Person with PartyScene =>

  private var lastMessages: List[Message] = Nil

  protected def findByNick(nick: String): Option[Person with Contact] = contacts.find(_.nick == nick)

  protected def onMessageReceived(message: Message): Unit = {
    addMessage(message)
    lastMessages ::= message
    self.onMessage(message)
  }

  protected def scanContactsForPhone(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = {
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

trait PartyScene {

  def contacts: List[Person with Contact]

  def self: Person with MessageListener

}


@dimension
trait Contact {
  def message(msg: String): Unit

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String]
}

@dimension
trait OtherSide {
  def theOther: Person
}

object DummyOtherSide extends OtherSide {
  def theOther: Person = ???
}


class OtherSideImpl(val theOther: Person) extends OtherSide

@fragment
trait RealOtherSide extends dlg[OtherSide] {
}

@fragment
trait Friend extends Contact {
  this: Person with Party with PartyScene with OtherSide =>

  override def message(msg: String): Unit = {
    onMessageReceived(Message(msg, theOther, new Date()))
  }

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = {
    if (target.nick == self.nick) {
      if (examineRequester(onBehalfOf)) {
        Some(phone)
      } else {
        None
      }
    } else {
      scanContactsForPhone(target, onBehalfOf, path)
    }
  }

  private def examineRequester(requester: Person): Boolean = {
    true
  }

}

@fragment
trait Associate extends Contact {
  this: Party with OtherSide =>

  override def message(msg: String): Unit = {
    onMessageReceived(Message(msg, theOther, new Date()))
  }

  def phoneRequest(target: Person, onBehalfOf: Person, path: List[Person]): Option[String] = None
}

trait Status extends MessageListener {
}

@fragment
trait Online extends Status {
  this: Person with Party with PartyScene with \?[Offline] =>

  def sendMessageTo(nick: String, msg: String) = {
    for (target <- findByNick(nick))
      target.message(msg)
  }

  def askForPhone(nick: String): Option[String] = {
    for (target <- findByNick(nick);
         phone <- scanContactsForPhone(target, self, Nil)) yield phone
  }

  override def onMessage(message: Message): Unit = {
    println(s"Message received from ${message.from.nick}:")
    println(message.message)
  }

  def disconnect(): Unit = {
    for (m <- mirror(this);
         mm <- m.owningMutableProxy) {
      val ci = m.kernel
      val offlineStrategy = promote[Offline](ci.defaultStrategy, 0)
      mm.remorph(offlineStrategy)
      require(select[Offline](mm).isDefined)
    }
  }

}

@fragment @wrapper
trait OnlineGuard extends Online {
  this: Person with Party with PartyScene with \?[Offline] =>

  override def sendMessageTo(nick: String, msg: String): Unit = {
    checkOnline()
    super.sendMessageTo(nick, msg)
  }

  override def askForPhone(nick: String): Option[String] = {
    checkOnline()
    super.askForPhone(nick)
  }

  override def onMessage(message: Message): Unit = {
    checkOnline()
    super.onMessage(message)
  }

  override def disconnect(): Unit = {
    if (isOnline) {
      super.disconnect()
    }
  }

  private def checkOnline(): Unit = {
    require(isOnline, s"Party $nick is offline")
  }

  private def isOnline: Boolean = {
    (for (m <- mirror(this);
          mm <- m.owningMutableProxy) yield {
      select[Online](mm).isDefined
    }) match {
      case None => false
      case Some(online) => online
    }
  }

}

@fragment
trait Offline extends Status {
  override def onMessage(message: Message): Unit = {}
}

@dimension
trait MessageListener {
  def onMessage(message: Message): Unit
}

object Context {
  type PartyMorphType = Person with Party with PartyScene with
    (((Friend or
      Associate) with OtherSide) or
      (Offline or (Online with OnlineGuard)))
  val partyMorphModel = parse[PartyMorphType](true)
}

class Context(network: List[Person]) {

  import Context._

  private val partyKernels: Map[String, partyMorphModel.Kernel] = network.map(createParty).toMap
  private lazy val parties: Map[String, Person with Status with MutableMorphMirror[Person with (Offline or Online)]] = partyKernels.map(partyKernel => {
    val partyKernelRef: &[Person with (Offline or Online)] = partyKernel._2
    val p = *(partyKernelRef).~
    (partyKernel._1, p)
  })

  private def createParty(person: Person): (String, partyMorphModel.Kernel) = {
    implicit val personFrag = external(person)
    implicit val partySceneFrag = external[PartyScene](new PartySceneImpl(person))
    implicit val otherSideFrag = external[OtherSide](DummyOtherSide)
    val partyKernel = singleton(partyMorphModel, rootStrategy(partyMorphModel))
    (person.nick, partyKernel)
  }

  private class PartySceneImpl(person: Person) extends PartyScene {
    lazy val contacts: List[Person with Contact] = for (conn <- person.connections) yield {
      partyKernels.get(conn.person.nick) match {
        case None => sys.error(s"Unidentified person ${conn.person.nick}")
        case Some(contactKernel) =>
          val otherSideReplacement = single[RealOtherSide, OtherSideImpl](new OtherSideImpl(person))
          if (conn.person.isTrusted(person)) {
            asMorphOf[Person with Friend with $[RealOtherSide]](contactKernel, otherSideReplacement)
          } else
            asMorphOf[Person with Associate with $[RealOtherSide]](contactKernel, otherSideReplacement)
      }
    }

    lazy val self = parties(person.nick)
  }

  def connect(selfPartyNick: String): Option[Person with Online] = {
    for (selfParty <- parties.get(selfPartyNick)) yield {
      val onlineStrategy = promote[Online](selfParty.strategy, 0)
      selfParty.remorph(onlineStrategy)
      select[Person with Online](selfParty) match {
        case None => sys.error("")
        case Some(online) => online
      }
    }
  }

}

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

    def connectParty(nick: String): Person with Online = ctx.connect(nick) match {
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

    connected1.disconnect()
    connected3.disconnect()

    connected1.sendMessageTo("person3", "Hello2")
    connected3.sendMessageTo("person1", "Hello3")

  }

}