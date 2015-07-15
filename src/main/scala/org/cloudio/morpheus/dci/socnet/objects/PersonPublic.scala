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

  def addConnection(connection: Connection): Unit

  def removeConnection(nick: String): Unit

  def trustedOnly: List[Connection] = connections.filter(_.trusted)

  def isTrusted(nick: String): Boolean = trustedOnly.exists(_.personNick == nick)

}

trait PersonJobs {
  def addJob(job: Job): Unit

  def removeJob(job: Job): Unit

  def jobs: Iterable[Job]
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

  override def removeConnection(nick: String): Unit = conn = conn.filterNot(_.personNick == nick)

}



@fragment
trait SomeoneJobs extends PersonJobs {
  private var jobList: List[Job] = Nil

  override def addJob(job: Job): Unit = jobList ::= job

  override def removeJob(job: Job): Unit = jobList = jobList.filterNot(_ == job)
  
  override def jobs: Iterable[Job] = jobList
}


case class Address(city: String, street: String, country: String)

case class Connection(personNick: String, trusted: Boolean)

case class Job(company: String, position: String, from: Date, until: Option[Date])


// The objective morph model expresses all valid forms.
object Person {
  val personMorphModel = parse[
      SomeonePublic with
      \?[SomeonePrivate] with // a person is valid (usable) without its private data
      SomeoneConnections with
      SomeoneJobs with
      (Offline or Online)](true) // the objective state fragments
}

