package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/06/15.
 */

// Pure data parts

case class PersonPublic(nick: String, firstName: String, lastName: String, email: String)

case class Address(city: String, street: String, country: String)

case class PersonPrivate(phone: String, address: Address)

case class Connection(personNick: String, trusted: Boolean)

case class Job(company: String, position: String, from: Date, until: Option[Date])

// Entity fragments holding the data parts

@fragment
trait PersonPublicEntity {

  protected var personPublic: PersonPublic = _

  def nick = personPublic.nick

  def firstName = personPublic.firstName

  def lastName = personPublic.lastName

  def email = personPublic.email

}


@fragment
trait PersonPrivateEntity {

  protected var personPrivate: PersonPrivate = _

}

@fragment
trait PersonConnectionsEntity {
  this: PersonPublicEntity =>

  protected var connections: List[Connection] = Nil

  def allConnections = connections

  def trustedOnly: List[Connection] = connections.filter(_.trusted)

  def isTrusted(nick: String): Boolean = trustedOnly.exists(_.personNick == nick)

  def removeConnection(nick: String): Unit = connections = connections.filterNot(_.personNick == personPublic.nick)

}

@fragment
trait PersonJobsEntity {
  this: PersonPublicEntity =>

  protected var jobs: List[Job] = Nil

  def isColleague(other: PersonPublicEntity with PersonJobsEntity): Boolean = {
    other.nick != personPublic.nick &&
      other.jobs.exists(j1 => jobs.exists(j2 => j1.company == j2.company))
  }
}

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

case class Perception(network: String, subjectNick: String, subjectRole: String, objectNick: String, objectRole: String)

@fragment
trait NodeStats {
  this: PersonPublicEntity =>

  protected var perceivedBy: List[Perception] = Nil
  protected var perceives: List[Perception] = Nil

  def subjectPerceptions = perceives

  def objectPerceptions = perceivedBy

  def addSubjectPerception(network: String, subjectRole: String, objectNick: String, objectRole: String): Unit = {
    perceives ::= Perception(network, nick, subjectRole, objectNick, objectRole)
  }

  def addObjectPerception(network: String, subjectNick: String, subjectRole: String, objectRole: String): Unit = {
    perceives ::= Perception(network, subjectNick, subjectRole, nick, objectRole)
  }

  def removeSubjectPerceptions(network: String): Unit = {
    perceives = perceives.filterNot(_.network == network)
  }

  def removeObjectPerceptions(network: String, subjectNick: String): Unit = {
    perceives = perceivedBy.filterNot(p => p.network == network && p.subjectNick == subjectNick)
  }
}

// Morph Model
object Person {

  type PersonType = PersonPublicEntity with
    \?[PersonPrivateEntity] with // the private data are not provided to anyone
    PersonConnectionsEntity with
    PersonJobsEntity with
    (Offline or Online) with
    NodeStats

  val personMorphModel = parse[PersonType](true)

  val statusSwitch: (Option[personMorphModel.MutableLUB]) => Option[Int] =
    for (person <- _) yield {
      if (person.subjectPerceptions.isEmpty) 0 else 1
    }

  def newPerson(): personMorphModel.Kernel = {
    val sw: Option[personMorphModel.MutableLUB] => Int = null
    val strategy = promote[Offline or Online](personMorphModel)(statusSwitch)
    singleton(personMorphModel, strategy)
  }

}
