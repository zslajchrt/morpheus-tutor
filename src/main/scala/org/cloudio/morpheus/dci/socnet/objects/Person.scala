package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

/**
 * Created by zslajchrt on 29/06/15.
 */
trait Person {

  def nick: String

  // public data

  def firstName: String

  def lastName: String

  def email: String

  def connections: List[Connection]

  def trustedOnly(): List[Connection] = connections.filter(_.trusted)

  def isTrusted(person: Person): Boolean = trustedOnly().exists(_.person.nick == person.nick)

  // protected data

  protected def phone: String

  protected def address: Address

  protected def addConnection(connection: Connection): Unit

  protected def removeConnection(nick: String): Unit

  protected def addMessage(message: Message): Unit

  protected def removeMessage(message: Message): Unit

  protected def messages(message: Message): Iterable[Message]

}

class Someone(val nick: String, val firstName: String, val lastName: String, val email: String, phoneNumber: String, addr: Address) extends Person {
  override protected def phone: String = phoneNumber

  override protected def address: Address = addr

  private var conn: List[Connection] = Nil
  private var msgs: List[Message] = Nil

  override def connections: List[Connection] = conn

  override def addConnection(connection: Connection): Unit = conn ::= connection

  override protected def removeConnection(nick: String): Unit = conn = conn.filterNot(_.person.nick == nick)

  override protected def addMessage(message: Message): Unit = msgs ::= message

  override protected def removeMessage(message: Message): Unit = msgs = msgs.filterNot(_ == message)

  override protected def messages(message: Message): Iterable[Message] = msgs
}

case class Address(city: String, street: String, country: String)

case class Connection(person: Person, trusted: Boolean)

case class Message(message: String, from: Person, received: Date)