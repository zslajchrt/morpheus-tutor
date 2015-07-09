package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/06/15.
 */
trait PersonPublic {

  def nick: String

  def firstName: String

  def lastName: String

  def email: String

}

case class PersonPublicInit(nick: String, firstName: String, lastName: String, email: String) extends PersonPublic

trait PersonPrivate {

  def phone: String

  def address: Address

}

case class PersonPrivateInit(phone: String, address: Address) extends PersonPrivate

trait PersonConnections {
  this: PersonPublic =>

  def connections: List[Connection]

  def connectedPerson(nick: String): PersonPublic with PersonConnections

  def addConnection(connection: Connection): Unit

  def removeConnection(nick: String): Unit

  def trustedOnly: List[Connection] = connections.filter(_.trusted)

  def isTrusted(nick: String): Boolean = trustedOnly.exists(_.person.nick == nick)

  // friendship is a bidirectional relationship, i.e. I trust you, you trust me
  def friends = trustedOnly.filter(tc => connectedPerson(tc.person.nick).isTrusted(nick))

}

trait PersonMessages {
  def addMessage(message: Message): Unit

  def removeMessage(message: Message): Unit

  def messages(message: Message): Iterable[Message]
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


@fragment
trait SomeonePublic extends dlg[PersonPublic] {
}


@fragment
trait SomeonePrivate extends dlg[PersonPrivate] {
}

@fragment
trait SomeoneConnections extends PersonConnections {
  this: PersonPublic =>

  private var conn: List[Connection] = Nil

  override def connections: List[Connection] = conn

  override def addConnection(connection: Connection): Unit = conn ::= connection

  override def removeConnection(nick: String): Unit = conn = conn.filterNot(_.person.nick == nick)

  override def connectedPerson(nick: String): PersonPublic with PersonConnections = ???
}



@fragment
trait SomeoneMessages extends PersonMessages {
  private var msgs: List[Message] = Nil

  override def addMessage(message: Message): Unit = msgs ::= message

  override def removeMessage(message: Message): Unit = msgs = msgs.filterNot(_ == message)

  override def messages(message: Message): Iterable[Message] = msgs
}


case class Address(city: String, street: String, country: String)

case class Connection(person: PersonPublic with PersonConnections, trusted: Boolean)

case class Message(message: String, from: PersonPublic, received: Date)


// The objective morph model expresses all valid forms.
object Person {
  val personMorphModel = parse[
      SomeonePublic with
      \?[SomeonePrivate] with // a person is valid (usable) without its private data
      \?[SomeoneConnections] with
      \?[SomeoneMessages] with
      (Offline or Online)](true) // the objective state fragments
}

