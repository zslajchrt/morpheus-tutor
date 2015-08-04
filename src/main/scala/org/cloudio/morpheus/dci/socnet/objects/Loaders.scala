package org.cloudio.morpheus.dci.socnet.objects

import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.{DefaultFormats, JString, JValue}
import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._
import FragmentValidator._

/**
 * Created by zslajchrt on 21/07/15.
 */

@dimension
trait PersonLoader[T] {

  def load(personDoc: T): ValidationResult[_]
}

@fragment
trait JsonPersonPublicLoader extends PersonLoader[JValue] {
  this: PersonPublicEntity =>

  override def load(personJson: JValue) = {
    implicit val formats = DefaultFormats
    personPublic = personJson.\("public").extract[PersonPublic]
    success[PersonPublicEntity]
  }
}

@fragment
trait JsonPersonPrivateLoader extends PersonLoader[JValue] {
  this: PersonPrivateEntity =>

  override def load(personJson: JValue) = {
    implicit val formats = DefaultFormats
    personJson.\("private").extractOpt[PersonPrivate] match {
      case Some(privateData) =>
        personPrivate = privateData
        success[PersonPrivateEntity]

      case None => failure[PersonPrivateEntity]("invalid content")

    }
  }
}


@fragment
trait JsonPersonPrivateLoaderV1_0 extends PersonLoader[JValue] {
  this: PersonPrivateV1_0Entity =>

  override def load(personJson: JValue) = {
    implicit val formats = DefaultFormats
    personJson.\("private").extractOpt[PersonPrivateV1_0] match {
      case Some(privateDataV1_0) =>
        personPrivateV1_0 = PersonPrivateV1_0(privateDataV1_0.phone, privateDataV1_0.country, privateDataV1_0.city, privateDataV1_0.street)
        success[PersonPrivateV1_0Entity]

      case None => failure[PersonPrivateV1_0Entity]("invalid content")
    }
  }
}

@fragment
trait JsonPersonConnectionsLoader extends PersonLoader[JValue] {
  this: PersonConnectionsEntity =>

  override def load(personJson: JValue) = {
    implicit val formats = DefaultFormats
    connections = personJson.\("connections").extract[List[Connection]]

    success[PersonConnectionsEntity]
  }
}

@fragment
trait JsonPersonJobsLoader extends PersonLoader[JValue] {
  this: PersonJobsEntity =>

  override def load(personJson: JValue) = {
    implicit val formats = DefaultFormats
    jobs = personJson.\("jobs").extract[List[Job]]

    success[PersonJobsEntity]
  }
}

@fragment
trait JsonPersonAdStatsLoader extends PersonLoader[JValue] {
  this: PersonAdStatsEntity =>

  override def load(personJson: JValue) = {
    //failure[PersonAdStatsEntity]("invalid content")
    success[PersonAdStatsEntity]
  }
}


object JsonLoaders {

  type PersonLoaders = $[JsonPersonPublicLoader or
    JsonPersonPrivateLoader or
    JsonPersonPrivateLoaderV1_0 or
    JsonPersonConnectionsLoader or
    JsonPersonJobsLoader or
    JsonPersonAdStatsLoader]

//  type PersonLoaders = $[JsonPersonPrivateLoaderV1_0]

  val loadersModel = parseRef[PersonLoaders]

  val loaderFactoriesKernel = singleton_?[$[PersonLoaders]]
  val loaderFactories = tupled(loaderFactoriesKernel)

  def load(pl: &[PersonLoaders], personJson: JValue): Set[Int] = {
    val plKernel = *(pl, loaderFactories)
    val failedFragments = for (loader <- plKernel;
                               loaderResult = loader.load(personJson)
                               if !loaderResult.succeeded;
                               frag <- loaderResult.fragment) yield frag.index

    failedFragments.toSet
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
    var failedFragments: Option[Set[Int]] = None
    val p = newPerson(MaskExplicitStrategy(rootStrategy(Person.personMorphModel), true, () => failedFragments))

    val res = getClass.getClassLoader.getResourceAsStream(s"persons/person$id.json")
    failedFragments = Some(JsonLoaders.load(p, JsonMethods.parse(res)))

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

