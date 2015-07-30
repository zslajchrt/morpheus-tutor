package org.cloudio.morpheus.dci.socnet.objects

import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.{DefaultFormats, JString, JValue}
import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 21/07/15.
 */

@dimension
trait PersonLoader[T] {
  def load(personDoc: T): Unit
}

@fragment
trait JsonPersonPublicLoader extends PersonLoader[JValue] {
  this: PersonPublicEntity =>

  override def load(personJson: JValue): Unit = {
    implicit val formats = DefaultFormats
    personPublic = personJson.\("public").extract[PersonPublic]
  }
}

@fragment
trait JsonPersonPrivateLoader extends PersonLoader[JValue] {
  this: PersonPrivateEntity =>

  override def load(personJson: JValue): Unit = {
    implicit val formats = DefaultFormats
    personJson.\("private").extractOpt[PersonPrivate] match {
      case Some(privateData) => personPrivate = privateData
      case None =>
    }
  }
}


case class PersonPrivateV1_0(phone: Option[String], country: String, city: String, street: String)

@fragment
trait JsonPersonPrivateLoaderV1_0 extends PersonLoader[JValue] {
  this: PersonPrivateEntity =>

  override def load(personJson: JValue): Unit = {
    implicit val formats = DefaultFormats
    personJson.\("private").extractOpt[PersonPrivateV1_0] match {
      case Some(privateDataV1_0) =>
        personPrivate = PersonPrivate(privateDataV1_0.phone.getOrElse(""),
          Address(privateDataV1_0.city, privateDataV1_0.street, privateDataV1_0.country))
      case None =>
    }
  }
}

@fragment
trait JsonPersonConnectionsLoader extends PersonLoader[JValue] {
  this: PersonConnectionsEntity =>

  override def load(personJson: JValue): Unit = {
    implicit val formats = DefaultFormats
    connections = personJson.\("connections").extract[List[Connection]]
  }
}

@fragment
trait JsonPersonJobsLoader extends PersonLoader[JValue] {
  this: PersonJobsEntity =>

  override def load(personJson: JValue): Unit = {
    implicit val formats = DefaultFormats
    jobs = personJson.\("jobs").extract[List[Job]]
  }
}

object JsonLoaders {

  type PersonLoaders = $[JsonPersonPublicLoader] or
    $[JsonPersonPrivateLoader] or
    $[JsonPersonPrivateLoaderV1_0] or
    $[JsonPersonConnectionsLoader] or
    $[JsonPersonJobsLoader]

  val loadersModel = parseRef[PersonLoaders]

  def load(pl: &[PersonLoaders], personJson: JValue): loadersModel.Kernel = {
    val plKernel = *(pl,
      single[JsonPersonPublicLoader],
      single[JsonPersonPrivateLoader],
      single[JsonPersonPrivateLoaderV1_0],
      single[JsonPersonConnectionsLoader],
      single[JsonPersonJobsLoader])

    for (loader <- plKernel) {
      loader.load(personJson)
    }

    plKernel
  }

  def reload(plKernel: loadersModel.Kernel, personJson: JValue): Unit = {
    for (loader <- plKernel) {
      loader.load(personJson)
    }
  }

}

object PersonSample {

  import Person._

  def loadPerson(id: Int): personMorphModel.Kernel = {
    val res = getClass.getClassLoader.getResourceAsStream(s"persons/person$id.json")
    val p = newPerson()
    JsonLoaders.load(p, JsonMethods.parse(res))
    p
  }

  val persons = List(loadPerson(1), loadPerson(2), loadPerson(3), loadPerson(4))
  val personsAsMap = persons.map(pk => (pk.~.nick, pk)).toMap

//  def main(args: Array[String]) {
//    val j = JsonMethods.parse(getClass.getClassLoader.getResourceAsStream(s"persons/person1.json"))
//    val p1: JsonLoaders.loadersModel.Kernel = JsonLoaders.load(newPerson(), j)
//
//    val ts1 = System.currentTimeMillis()
//
//    val allLoaders = morphKernelToIterable(p1).toList
//
//    for (i <- 0 to 1000) {
//      for (loader <- allLoaders) {
//        loader.load(j)
//      }
//    }
//    val ts2 = System.currentTimeMillis()
//    println(s"Time spent: ${ts2 - ts1}")
//
//  }
}
