package org.cloudio.morpheus.ontouml

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import org.morpheus.Morpheus._
import org.morpheus._

/**
  * Created by zslajchrt on 23/04/16.
  */
object Entities {

}

@fragment
trait Brain {

}

@fragment
trait Heart {

}

trait PersonalData {
  val personId: Long
  var firstName: String
  var lastName: String
  val birthDate: Date
}

@fragment
trait Person extends dlg[PersonalData] {
  val brain = singleton[Brain].!
  // essential
  var heart = singleton[Heart].! // replaceable

  def age = {
    val diffInMillis = new Date().getTime - birthDate.getTime
    val a = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) / 365
    a
  }
}

case class PersonalDataParams(val personId: Long, fn: String, ln: String, val birthDate: Date) extends PersonalData {
  override var firstName: String = fn
  override var lastName: String = ln
}

@fragment
trait Man {
  this: Person =>
}

@fragment
trait Woman {
  this: Person =>
}

@fragment
trait Child {
  this: Person =>
}

@fragment
trait Teenager {
  this: Person =>
}

@fragment
trait Adult {
  this: Person =>
}

@fragment
trait Student {
  this: Person =>

  var credits: Int = _
}

@fragment
trait Employee {
  this: Person =>
}

trait OrganizationData {
  val organizationId: Long
  var organizationName: String
}

@fragment
trait Organization extends dlg[OrganizationData] {

}

case class OrganizationDataParams(val organizationId: Long, nm: String) extends OrganizationData {
  override var organizationName: String = nm
}

@fragment
trait HiringOrganization {
  this: Organization =>
}

@fragment
trait School {
  this: Organization =>

  var schoolRating: Float = _
}


object Models {

  type OrganizationModel = Organization *
    /?[School] *
    /?[HiringOrganization]

  val organizationModel = parse[OrganizationModel](true)

  type PersonModel = Person *
    (Man | Woman) *
    (Child | Teenager | Adult) *
    /?[Student] *
    /?[Employee]

  val personModel = parse[PersonModel](true)

}


class Enrollment(studentRef: &[Student with Person], schoolRef: &[School with Organization]) {
  val student: Student with Person = *(studentRef).~
  val school: School with Organization = *(schoolRef).~
}

class Payroll(employeeRef: &[Employee with Person], hiringOrgRef: &[HiringOrganization with Organization]) {
  val employee: Employee with Person = *(employeeRef).!
  val hiringOrg: HiringOrganization with Organization = *(hiringOrgRef).!
}

import org.cloudio.morpheus.ontouml.Models._

object App {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  // tables
  var enrollments: Map[Long, List[Enrollment]] = Map.empty
  var payrolls: Map[Long, List[Payroll]] = Map.empty

  def createOrganization(orgId: Long, name: String) = {
    implicit val orgFact = single[Organization, OrganizationData](OrganizationDataParams(orgId, name))
    singleton(organizationModel, rootStrategy(organizationModel))
  }

  def createPerson(personId: Long, firstName: String, lastName: String, birthDate: String, woman: Boolean) = {

    var stg: MorphingStrategy[personModel.Model] = rootStrategy(personModel)

    stg = maskAll(stg)

    stg = mask[Woman | Man](stg, () => if (woman) Some(0) else Some(1))

    stg = maskFull[Child | Teenager | Adult](personModel)(stg, {
      case Some(person) if person.age < 15 => Some(0)
      case Some(person) if person.age < 18 => Some(1)
      case Some(person) if person.age >= 18 => Some(2)
      case None => None
    })

    stg = unmaskFull[Student](personModel)(stg, {
      case Some(person) if !enrollments.contains(person.personId) => Some(0)
      case _ => None
    })

    stg = unmaskFull[Employee](personModel)(stg, {
      case Some(person) if !payrolls.contains(person.personId) => Some(0)
      case _ => None
    })

    implicit val personFact = single[Person, PersonalData](PersonalDataParams(personId, firstName, lastName, dateFormat.parse(birthDate)))
    val rec = singleton(personModel, stg)
    rec.~.remorph // now the strategies will get the person reference
    rec
  }

  def enroll(person: personModel.Recognizer, schoolRef: &[School with Organization]) = {
    enrollments.get(person.~.personId) match {
      case None =>
        enrollments += (person.~.personId -> Nil) // The student strategy will recognize the person as a student
    }
    val enrollment = new Enrollment(person, schoolRef)
    enrollments += (person.~.personId -> (enrollment :: enrollments(person.~.personId)))
    person.~.remorph

    enrollment
  }

  def dismiss(person: personModel.Recognizer, from: Organization): Unit = {
    enrollments.get(person.~.personId) match {
      case None =>
      case Some(existingEnr) =>
        val newEnr = existingEnr.filter(_.school.organizationId != from.organizationId)
        if (newEnr.isEmpty)
          enrollments -= person.~.personId
        else
          enrollments += (person.~.personId -> newEnr)
    }
    person.~.remorph
  }

  def employ(person: personModel.Recognizer, hiringOrgRef: &[HiringOrganization with Organization]) = {
    payrolls.get(person.~.personId) match {
      case None =>
        payrolls += (person.~.personId -> Nil)
    }
    val payroll = new Payroll(person, hiringOrgRef)
    payrolls += (person.~.personId -> (payroll :: payrolls(person.~.personId)))
    person.~.remorph

    payroll
  }

  def fire(person: personModel.Recognizer, from: Organization): Unit = {
    payrolls.get(person.~.personId) match {
      case None =>
      case Some(existingEmp) =>
        val newEmp = existingEmp.filter(_.hiringOrg.organizationId != from.organizationId)
        if (newEmp.isEmpty)
          payrolls -= person.~.personId
        else
          payrolls += (person.~.personId -> newEmp)
    }
    person.~.remorph
  }

  def main(args: Array[String]) {
    val personRec = createPerson(123, "John", "Doe", "2000-06-02", woman = false)
    val organizationRec = createOrganization(1000, "VŠE")

    println(personRec.~.myAlternative)
    println(organizationRec.~.myAlternative)

    //new Enrollment(personRec, organizationRec)
    //new Payroll(personRec, organizationRec)

    val enr: Enrollment = enroll(personRec, organizationRec)
    println(s"${enr.student.firstName}:${enr.student.lastName}:${enr.student.personId}:${enr.student.birthDate}:${enr.student.credits}:${enr.school.schoolRating}")

    println(personRec.~.myAlternative)

    employ(personRec, organizationRec)
    println(personRec.~.myAlternative)

    fire(personRec, organizationRec.~)
    println(personRec.~.myAlternative)

    dismiss(personRec, organizationRec.~)
    println(personRec.~.myAlternative)

  }


}