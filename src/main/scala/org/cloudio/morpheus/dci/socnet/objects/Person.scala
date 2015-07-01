package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/06/15.
 */
trait Person {

  def nick: String

  def firstName: String

  def lastName: String

  def email: String

}

trait PersonPrivate {

  def phone: String

  def address: Address

}

trait PersonConnections {
  this: Person =>

  def connections: List[Connection]

  protected def connectedPerson(nick: String): Person with PersonConnections

  protected def addConnection(connection: Connection): Unit

  protected def removeConnection(nick: String): Unit

  def trustedOnly: List[Connection] = connections.filter(_.trusted)

  def isTrusted(nick: String): Boolean = trustedOnly.exists(_.person.nick == nick)

  // friendship is a bidirectional relationship, i.e. I trust you, you trust me
  def friends = trustedOnly.filter(tc => connectedPerson(tc.person.nick).isTrusted(nick))

}

trait PersonMessages {
  protected def addMessage(message: Message): Unit

  protected def removeMessage(message: Message): Unit

  protected def messages(message: Message): Iterable[Message]
}

// faces

@dimension
trait Status {
  def isOnline: Boolean
}

@fragment
trait Offline extends Status {
  override def isOnline: Boolean = false
}

@fragment
trait Online extends Status {
  override def isOnline: Boolean = true
}

case class PersonInit(nick: String, firstName: String, lastName: String, email: String) extends Person

@fragment
trait Someone extends dlg[Person] {
}

case class PersonPrivateInit(phone: String, address: Address) extends PersonPrivate

@fragment
trait SomeonePrivate extends dlg[PersonPrivate] {
}

@fragment
trait SomeoneConnections extends PersonConnections {
  this: Person =>

  private var conn: List[Connection] = Nil

  override def connections: List[Connection] = conn

  override def addConnection(connection: Connection): Unit = conn ::= connection

  override protected def removeConnection(nick: String): Unit = conn = conn.filterNot(_.person.nick == nick)

  override protected def connectedPerson(nick: String): Person with PersonConnections = ???
}



@fragment
trait SomeoneMessages extends PersonMessages {
  private var msgs: List[Message] = Nil

  override protected def addMessage(message: Message): Unit = msgs ::= message

  override protected def removeMessage(message: Message): Unit = msgs = msgs.filterNot(_ == message)

  override protected def messages(message: Message): Iterable[Message] = msgs
}


case class Address(city: String, street: String, country: String)

case class Connection(person: Person with PersonConnections, trusted: Boolean)

case class Message(message: String, from: Person, received: Date)


object Person {
  type P = SomeonePrivate
  val personMorphModel = parse[Someone with \?[SomeonePrivate] with SomeoneConnections with SomeoneMessages with (Offline or Online)](true)
}

import Person._

