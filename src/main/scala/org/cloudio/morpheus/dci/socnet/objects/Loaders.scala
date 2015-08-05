package org.cloudio.morpheus.dci.socnet.objects

import org.json4s.JsonAST.{JNothing, JArray, JObject}
import org.json4s.{Formats, DefaultFormats, JString, JValue}
import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._
import FragmentValidator._

/**
 * Created by zslajchrt on 21/07/15.
 */

@dimension
trait PersonLoader[T] {

  def load: ValidationResult[_]
}

//object PersonLoader {
//  import scala.language.experimental.macros
//  import scala.reflect.macros.whitebox
//
//  def extract[F, D](src: JValue, attr: String): ValidationResult[F] = macro extract_impl[F, D]
//
//  def extract_impl[F: c.WeakTypeTag, D: c.WeakTypeTag](c: whitebox.Context)(src: c.Tree, attr: c.Tree): c.Tree = {
//    import c.universe._
//
//    val fTpe = implicitly[WeakTypeTag[F]]
//    val dTpe = implicitly[WeakTypeTag[D]]
//
//    q"""
//       $src.\($attr).extractOpt[$dTpe] match {
//          case Some(x) =>
//            success[$fTpe]
//          case None =>
//            failure[$fTpe]("invalid content")
//       }
//    """
//  }
//
//}

@fragment
trait JsonRegisteredUserLoader extends PersonLoader[JValue] {
  this: UserProtodata with RegisteredUserEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    registeredUserJson.\("public").extractOpt[RegisteredUser] match {
      case Some(ru) =>
        regUser = ru
        success[RegisteredUserEntity]
      case None =>
        failure[RegisteredUserEntity]("invalid content")
    }
  }
}


@fragment
trait JsonEmployeeLoader extends PersonLoader[JValue] {
  this: UserProtodata with EmployeeEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    employeeJson.\("employee").extractOpt[Employee] match {
      case Some(empData) =>
        emp = empData
        success[EmployeeEntity]
      case None =>
        failure[EmployeeEntity]("invalid content")
    }
  }
}

@fragment
trait JsonPersonPrivateLoaderV1_0 extends PersonLoader[JValue] {
  this: UserProtodata with PersonPrivateV1_0Entity =>

  override def load = {
    implicit val formats = DefaultFormats
    registeredUserJson.\("private").extractOpt[PersonPrivateV1_0] match {
      case Some(privateDataV1_0) =>
        personPrivateV1_0 = privateDataV1_0
        success[PersonPrivateV1_0Entity]
      case None => failure[PersonPrivateV1_0Entity]("invalid content")
    }
  }
}

@fragment
trait JsonPersonPrivateLoaderV2_0 extends PersonLoader[JValue] {
  this: UserProtodata with PersonPrivateV2_0Entity =>

  override def load = {
    implicit val formats = DefaultFormats
    registeredUserJson.\("private").extractOpt[PersonPrivateV2_0] match {
      case Some(privateData) =>
        personPrivate = privateData
        success[PersonPrivateV2_0Entity]

      case None => failure[PersonPrivateV2_0Entity]("invalid content")

    }
  }
}

@fragment
trait JsonPersonConnectionsLoader extends PersonLoader[JValue] {
  this: UserProtodata with PersonConnectionsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (connectionsJson == JNothing) {
      failure[PersonConnectionsEntity]("invalid content")
    } else
      connectionsJson.\("connections").extractOpt[List[Connection]] match {
        case Some(cons) =>
          connections = cons
          success[PersonConnectionsEntity]
        case None =>
          failure[PersonConnectionsEntity]("invalid content")
      }

  }
}

@fragment
trait JsonPersonJobsLoader extends PersonLoader[JValue] {
  this: UserProtodata with PersonJobsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (jobsJson == JNothing) {
      failure[PersonJobsEntity]("invalid content")
    } else
      jobsJson.\("jobs").extractOpt[List[Job]] match {
        case Some(j) =>
          jobs = j
          success[PersonJobsEntity]
        case None =>
          failure[PersonJobsEntity]("invalid content")
      }
  }
}

@fragment
trait JsonPersonAdStatsLoader extends PersonLoader[JValue] {
  this: UserProtodata with PersonAdStatsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (adCampaignJson == JNothing) {
      failure[PersonAdStatsEntity]("invalid content")
    } else
      adCampaignJson.\("campaigns").extractOpt[List[AdCampaign]] match {
        case Some(a) =>
          seenAds = a
          success[PersonAdStatsEntity]
        case None =>
          failure[PersonAdStatsEntity]("invalid content")
      }
  }
}

@dimension
trait UserProtodata {

  def registeredUserJson: JValue

  def employeeJson: JValue

  def adCampaignJson: JValue

  def jobsJson: JValue

  def connectionsJson: JValue

  def initSources(userId: Int)
}

@fragment
trait UserProtodataMock extends UserProtodata {

  private[this] var userId: Int = _

  private def loadJson(path: String): JValue = {
    getClass.getClassLoader.getResourceAsStream(path) match {
      case null => JNothing
      case res => JsonMethods.parseOpt(res).getOrElse(JNothing)
    }
  }

  lazy val registeredUserJson: JValue = loadJson(s"persons/person$userId.json")

  lazy val employeeJson: JValue = loadJson(s"persons/employee$userId.json")

  lazy val adCampaignJson: JValue = loadJson(s"persons/campaigns$userId.json")

  lazy val connectionsJson: JValue = loadJson(s"persons/connections$userId.json")

  lazy val jobsJson: JValue = loadJson(s"persons/jobs$userId.json")

  // clickstream... todo

  override def initSources(userId: Int): Unit = {
    this.userId = userId
  }
}

object JsonLoaders {

  type PersonLoaders = $[(JsonRegisteredUserLoader or
    JsonEmployeeLoader or
    JsonPersonPrivateLoaderV1_0 or
    JsonPersonPrivateLoaderV2_0 or
    JsonPersonConnectionsLoader or
    JsonPersonJobsLoader or
    JsonPersonAdStatsLoader) with UserProtodataMock]

  //
  //  type PersonLoaders = $[JsonEmployeeLoader with UserProtodataMock]

  def load(pl: &[PersonLoaders], userId: Int): Set[Int] = {
    val loaderFactoriesKernel = singleton_?[$[PersonLoaders]]
    val loaderFactories = tupled(loaderFactoriesKernel)
    val plKernel = *(pl, loaderFactories)
    plKernel.!.initSources(userId)
    val failedFragments = for (loader <- plKernel;
                               loaderResult = loader.load
                               if !loaderResult.succeeded;
                               frag <- loaderResult.fragment) yield frag.index

    failedFragments.toSet
  }

}


object PersonSample {

  import Person._

  def loadPerson(userId: Int): personMorphModel.Kernel = {
    var failedFragments: Option[Set[Int]] = None
    val p = newPerson(MaskExplicitStrategy(rootStrategy(Person.personMorphModel), true, () => failedFragments))

    failedFragments = Some(JsonLoaders.load(p, userId))

    p
  }

  val persons = List(loadPerson(1), loadPerson(2), loadPerson(3), loadPerson(4))
  val personsAsMap = persons.map(pk => (pk.~.nick, pk)).toMap

  println(personsAsMap.keys)

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

