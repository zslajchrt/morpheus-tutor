package org.cloudio.morpheus.dci.socnet.objects

import java.util.Date

import org.json4s.JsonAST.{JArray, JNothing}
import org.json4s.native.JsonMethods
import org.json4s.{DefaultFormats, JValue}
import org.morpheus.FragmentValidator._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 21/07/15.
 */

@dimension
trait FragmentLoader[F] {

  def load: ValidationResult[F]
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
trait RegisteredUserLoader extends FragmentLoader[RegisteredUserEntity] {
  this: UserProtodata with RegisteredUserEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    this.registeredUserJson.extractOpt[RegisteredUser] match {
      case Some(ru) =>
        this.regUser = ru
        success[RegisteredUserEntity]
      case None =>
        failure[RegisteredUserEntity]("invalid content")
    }
  }
}

@fragment
trait JsonPersonPrivateLoaderV1_0 extends FragmentLoader[PersonPrivateV1_0Entity] {
  this: UserProtodata with PersonPrivateV1_0Entity =>

  override def load = {
    implicit val formats = DefaultFormats
    this.registeredUserJson.\("private").extractOpt[PersonPrivateV1_0] match {
      case Some(privateDataV1_0) =>
        this.personPrivateV1_0 = privateDataV1_0
        success[PersonPrivateV1_0Entity]
      case None => failure[PersonPrivateV1_0Entity]("invalid content")
    }
  }
}

@fragment
trait JsonPersonPrivateLoaderV2_0 extends FragmentLoader[PersonPrivateV2_0Entity] {
  this: UserProtodata with PersonPrivateV2_0Entity =>

  override def load = {
    implicit val formats = DefaultFormats
    this.registeredUserJson.\("private").extractOpt[PersonPrivateV2_0] match {
      case Some(privateData) =>
        this.personPrivate = privateData
        success[PersonPrivateV2_0Entity]

      case None => failure[PersonPrivateV2_0Entity]("invalid content")

    }
  }
}

@fragment
trait EmployeeLoader extends FragmentLoader[EmployeeEntity] {
  this: UserProtodata with EmployeeEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    this.employeeJson.\("employee").extractOpt[Employee] match {
      case Some(empData) =>
        this.emp = empData
        success[EmployeeEntity]
      case None =>
        failure[EmployeeEntity]("invalid content")
    }
  }
}

@fragment
trait PersonConnectionsLoader extends FragmentLoader[PersonConnectionsEntity] {
  this: UserProtodata with PersonConnectionsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (connectionsJson == JNothing) {
      failure[PersonConnectionsEntity]("invalid content")
    } else
      this.connectionsJson.\("connections").extractOpt[List[Connection]] match {
        case Some(cons) =>
          this.connections = cons
          success[PersonConnectionsEntity]
        case None =>
          failure[PersonConnectionsEntity]("invalid content")
      }

  }
}

@fragment
trait PersonJobsLoader extends FragmentLoader[PersonJobsEntity] {
  this: UserProtodata with PersonJobsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (jobsJson == JNothing) {
      failure[PersonJobsEntity]("invalid content")
    } else
      this.jobsJson.\("jobs").extractOpt[List[Job]] match {
        case Some(j) =>
          this.jobs = j
          success[PersonJobsEntity]
        case None =>
          failure[PersonJobsEntity]("invalid content")
      }
  }
}

@fragment
trait PersonAdStatsLoader extends FragmentLoader[PersonAdStatsEntity] {
  this: UserProtodata with PersonAdStatsEntity =>

  override def load = {
    implicit val formats = DefaultFormats
    if (adCampaignsJson == JNothing) {
      failure[PersonAdStatsEntity]("invalid content")
    } else
      this.adCampaignsJson.\("campaigns").extractOpt[List[AdCampaign]] match {
        case Some(a) =>
          this.seenAds = a
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

  def jobsJson: JValue

  def connectionsJson: JValue

  def adCampaignsJson: JValue

  def clicks(from: Date, to: Date): Iterator[JValue]

  def initSources(userId: String)
}

@fragment
trait UserProtodataMock extends UserProtodata {

  private[this] var userId: String = _

  private def loadJson(path: String): JValue = {
    getClass.getClassLoader.getResourceAsStream(path) match {
      case null => JNothing
      case res => JsonMethods.parseOpt(res).getOrElse(JNothing)
    }
  }

  lazy val registeredUserJson: JValue = loadJson(s"persons/person$userId.json")

  lazy val employeeJson: JValue = loadJson(s"persons/employee$userId.json")

  lazy val adCampaignsJson: JValue = loadJson(s"persons/campaigns$userId.json")

  lazy val connectionsJson: JValue = loadJson(s"persons/connections$userId.json")

  lazy val jobsJson: JValue = loadJson(s"persons/jobs$userId.json")

  override def clicks(from: Date, to: Date): Iterator[JValue] = {
    val streamJson = loadJson(s"persons/clicks$userId.json")
    streamJson.\("stream") match {
      case JArray(cl) => cl.iterator
      case _ => Iterator.empty
    }
  }

  override def initSources(userId: String): Unit = {
    this.userId = userId
  }
}

object JsonLoaders {

  type PersonLoaders = $[(RegisteredUserLoader or
    EmployeeLoader or
    JsonPersonPrivateLoaderV1_0 or
    JsonPersonPrivateLoaderV2_0 or
    PersonConnectionsLoader or
    PersonJobsLoader or
    PersonAdStatsLoader) with UserProtodataMock]

  //
  //  type PersonLoaders = $[JsonEmployeeLoader with UserProtodataMock]

  def load(pl: &[PersonLoaders], userId: String): Set[Int] = {
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

  import PersonModel._

  def loadPerson(userId: String): personMorphModel.Kernel = {
    var failedFragments: Option[Set[Int]] = None
    val p = newPerson(MaskExplicitStrategy(rootStrategy(PersonModel.personMorphModel), true, () => failedFragments))

    failedFragments = Some(JsonLoaders.load(p, userId))

    p
  }

  val persons = List(loadPerson("1"), loadPerson("2"), loadPerson("3"), loadPerson("4"), loadPerson("5"))
  val personsAsMap = persons.map(pk => (pk.~.nick, pk)).toMap

  def main(args: Array[String]) {
    val regUserKernel = singleton[RegisteredUserEntity]
    println(regUserKernel.~.registeredUser)

    {
      val regUserLoaderRef: &[$[RegisteredUserLoader with UserProtodataMock]] = regUserKernel
      val regUserLoaderKernel = *(regUserLoaderRef, single[RegisteredUserLoader], single[UserProtodataMock])
      regUserLoaderKernel.~.initSources("4")
      regUserLoaderKernel.~.load match {
        case Failure(_, reason) =>
          sys.error(s"Cannot load registered user: $reason")
        case Success(_) =>
          println(regUserKernel.~.registeredUser)

      }

    }
    //////
    {
      val empKernel = singleton[EmployeeEntity]
      val empLoaderRef: &[$[EmployeeLoader with UserProtodataMock]] = empKernel
      // ...
    }

    //////
    {
      val regUserOrEmpKernel = singleton[RegisteredUserEntity or EmployeeEntity]
      val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserProtodataMock]] = regUserOrEmpKernel
      val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
        single[UserProtodataMock])
      regUserOrEmpLoaderKernel.~.initSources("5")

      for (loader <- regUserOrEmpLoaderKernel) {
        println(loader.myAlternative)
        loader.load
      }

      val regUserEnt = asMorphOf[RegisteredUserEntity](regUserOrEmpKernel)
      println(regUserEnt.registeredUser)
      val empEnt = asMorphOf[EmployeeEntity](regUserOrEmpKernel)
      println(empEnt.employee)
    }

    {
      //////
      val regUserOrEmpModel = parse[RegisteredUserEntity or EmployeeEntity](true)
      var failedFragments: Option[Set[Int]] = None
      val morphStrategy = MaskExplicitStrategy(rootStrategy(regUserOrEmpModel), true, () => failedFragments)
      val regUserOrEmpKernel = singleton(regUserOrEmpModel, morphStrategy)

      val regUserOrEmpLoaderRef: &[$[(RegisteredUserLoader or EmployeeLoader) with UserProtodataMock]] = regUserOrEmpKernel
      val regUserOrEmpLoaderKernel = *(regUserOrEmpLoaderRef, single[RegisteredUserLoader], single[EmployeeLoader],
        single[UserProtodataMock])
      regUserOrEmpLoaderKernel.~.initSources("6")

      //////
      failedFragments = Some((for (loader <- regUserOrEmpLoaderKernel;
                                   loaderResult = loader.load
                                   if !loaderResult.succeeded;
                                   frag <- loaderResult.fragment) yield frag.index).toSet)

      //      val regUserEnt = asMorphOf[RegisteredUserEntity](regUserOrEmpKernel)
      //      println(regUserEnt.registeredUser)
      //      val empEnt = asMorphOf[EmployeeEntity](regUserOrEmpKernel)
      //      println(empEnt.employee)

      try {
        regUserOrEmpKernel.~.remorph
        select[RegisteredUserEntity](regUserOrEmpKernel.~) match {
          case Some(regUserEnt) =>
            println(regUserEnt.registeredUser)
          case None =>
            select[EmployeeEntity](regUserOrEmpKernel.~) match {
              case Some(empEnt) => println(empEnt.employee)
              case None => require(false)
            }
        }
      }
      catch {
        case ae: NoViableAlternativeException =>
          println("Cannot load registered user or employee")
      }

    }

  }

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

