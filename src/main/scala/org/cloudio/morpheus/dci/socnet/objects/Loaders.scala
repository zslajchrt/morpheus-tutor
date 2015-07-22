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
    personPrivate = personJson.\("private").extract[PersonPrivate]
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
    $[JsonPersonConnectionsLoader] or
    $[JsonPersonJobsLoader]

  val loadersModel = parseRef[PersonLoaders]

  def load(pl: &[PersonLoaders], personJson: JValue): Unit = {
    //val personJson: JValue = JsonMethods.parse(jsonSrc)
    val plKernel = *(pl,
      single[JsonPersonPublicLoader],
      single[JsonPersonPrivateLoader],
      single[JsonPersonConnectionsLoader],
      single[JsonPersonJobsLoader])

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

}
