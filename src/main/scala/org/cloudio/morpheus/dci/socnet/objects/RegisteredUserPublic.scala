package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/06/15.
 */

// Pure data parts

//sealed trait Person
sealed trait PersonPublic

case class RegisteredUserPublic(nick: String, firstName: String, lastName: String, email: Option[String]) extends PersonPublic

case class RegisteredUserLicense(premium: Boolean, validFrom: Date, validTo: Date)

case class RegisteredUser(`public`: RegisteredUserPublic, license: Option[RegisteredUserLicense])


case class EmployeePersonalData(firstName: String, middleName: Option[String], lastName: String, title: String)

case class Employee(employeeCode: String, position: String, department: String, personalData: EmployeePersonalData) extends PersonPublic

case class Address(city: String, street: String, country: String)

sealed trait PersonPrivateData

case class PersonPrivateV1_0(phone: Option[String], country: String, city: String, street: String) extends PersonPrivateData

case class PersonPrivateV2_0(phone: String, address: Address) extends PersonPrivateData

case class Connection(personNick: String, trusted: Boolean)

case class Job(company: String, position: String, from: Date, until: Option[Date])

case class AdCampaign(title: String, url: String, date: Date, keywords: List[String])

// Entity fragments holding the data parts

@fragment
trait RegisteredUserEntity {

  protected var regUser: RegisteredUser = _

  def registeredUser = regUser
}

@fragment
trait EmployeeEntity {

  protected var emp: Employee = _

  def employee = emp
}

@fragment
trait PersonPublicCommon {

  this: RegisteredUserEntity or EmployeeEntity =>

  lazy val employeeData: PersonPublic = {
    List(
      for (pp <- select[RegisteredUserEntity](this)) yield pp.registeredUser.`public`,
      for (pp <- select[EmployeeEntity](this)) yield pp.employee
    ).find(_.isDefined).get.get
  }

  def nick = employeeData match {
    case RegisteredUserPublic(n, _, _, _) => n
    case Employee(code, _, _, _) => code
  }

  def firstName = employeeData match {
    case RegisteredUserPublic(_, fn, _, _) => fn
    case Employee(_, _, _, EmployeePersonalData(fn, _, _, _)) => fn
  }

  def lastName = employeeData match {
    case RegisteredUserPublic(_, _, ln, _) => ln
    case Employee(_, _, _, EmployeePersonalData(_, _, ln, _)) => ln
  }

  def email: Option[String] = employeeData match {
    case RegisteredUserPublic(_, _, _, em) => em
    case Employee(code, _, _, _) => Some(s"$code@thebigcompany.com")
  }

}

@fragment
trait PersonPrivateCommon {
  this: PersonPrivateV1_0Entity or PersonPrivateV2_0Entity =>

  lazy val privateData: PersonPrivateData = {
    List(
      for (pp <- select[PersonPrivateV2_0Entity](this)) yield pp.privateData,
      for (pp <- select[PersonPrivateV1_0Entity](this)) yield pp.privateData
    ).find(_.isDefined).get.get
  }

  def phone: Option[String] = privateData match {
    case PersonPrivateV1_0(phone, _, _, _) => phone
    case PersonPrivateV2_0(phone, _) => Some(phone)
  }
}

@fragment
trait PersonPrivateV1_0Entity {

  protected var personPrivateV1_0: PersonPrivateV1_0 = _

  def privateData = personPrivateV1_0
}

@fragment
trait PersonPrivateV2_0Entity {

  protected var personPrivate: PersonPrivateV2_0 = _

  def privateData = personPrivate
}

@fragment
trait PersonConnectionsEntity {
  this: PersonPublicCommon =>

  protected var connections: List[Connection] = Nil

  def allConnections = connections

  def trustedOnly: List[Connection] = connections.filter(_.trusted)

  def isTrusted(nick: String): Boolean = trustedOnly.exists(_.personNick == nick)

  def removeConnection(nick: String): Unit = connections = connections.filterNot(_.personNick == this.nick)

}

@fragment
trait PersonJobsEntity {
  this: PersonPublicCommon =>

  protected var jobs: List[Job] = Nil

  def isColleague(other: PersonPublicCommon with PersonJobsEntity): Boolean = {
    other.nick != this.nick &&
      other.jobs.exists(j1 => jobs.exists(j2 => j1.company == j2.company))
  }
}

@fragment
trait PersonAdStatsEntity {
  this: PersonPublicCommon =>

  protected var seenAds: List[AdCampaign] = Nil

  def addSeenAd(ad: AdCampaign): Unit = {
    seenAds ::= ad
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

  //  def newSession(nick: String):

}

case class Perception(network: String, subjectNick: String, subjectRole: String, objectNick: String, objectRole: String)

@fragment
trait NodeStats {
  this: PersonPublicCommon =>

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
object PersonModel {

  type PersonType = PersonPublicCommon with (RegisteredUserEntity or EmployeeEntity) with
    \?[PersonPrivateCommon with (PersonPrivateV2_0Entity or PersonPrivateV1_0Entity)] with
    \?[PersonConnectionsEntity] with
    \?[PersonJobsEntity] with
    \?[PersonAdStatsEntity]
  //with (Offline or Online) with NodeStats

  val personMorphModel = parse[PersonType](true)

  //  val statusSwitch: (Option[personMorphModel.MutableLUB]) => Option[Int] =
  //    for (person <- _) yield {
  //      if (person.subjectPerceptions.isEmpty) 0 else 1
  //    }

  def newPerson(strategy: MorphingStrategy[personMorphModel.Model]): personMorphModel.Kernel = {
    singleton(personMorphModel, strategy)
  }

}
