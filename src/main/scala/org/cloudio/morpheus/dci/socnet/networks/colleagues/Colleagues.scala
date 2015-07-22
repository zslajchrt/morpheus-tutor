package org.cloudio.morpheus.dci.socnet.networks.colleagues

import java.util.Date

import org.cloudio.morpheus.dci.socnet.objects.JsonLoaders.PersonLoaders
import org.cloudio.morpheus.dci.socnet.objects.Person._
import org.cloudio.morpheus.dci.socnet.objects._
import org.cloudio.morpheus.dci.socnet.networks._
import org.json4s.JValue
import org.json4s.native.JsonMethods
import org.morpheus.Morpheus._
import org.morpheus._


/**
 *
 * Created by zslajchrt on 29/06/15.
 */

// Roles

@fragment
trait EndUser {
  this: PersonPublicEntity with Scene with NodeStats =>

  def onLogin(): Unit = {
    for (c <- colleagues) {
      addSubjectPerception("Colleagues", "EndUser", c.nick, "Colleague")
      c.onLogin(this)
    }
  }

  def onLogout(): Unit = {
    removeSubjectPerceptions("Colleagues")
    for (c <- colleagues) {
      c.onLogout(this)
    }
  }
}

@fragment
trait Colleague {
  this: NodeStats =>

  def onLogin(endUser: EndUser with PersonPublicEntity): Unit =
    addObjectPerception("Colleagues", endUser.nick, "EndUser", "Colleague")

  def onLogout(endUser: EndUser with PersonPublicEntity): Unit =
    removeObjectPerceptions("Colleagues", endUser.nick)

}

// Roles Morph Model

object Model {

  // Morph type
  type RoleMorphType = PersonPublicEntity with PersonConnectionsEntity with PersonJobsEntity with $[Scene] with
    (($[EndUser] with PersonPrivateEntity) or
      $[Colleague])

  // Morph model
  val roleMorphModel = parseRef[RoleMorphType]

  // Faces
  type EndUserFace = EndUser with PersonPublicEntity with PersonPrivateEntity with PersonConnectionsEntity with PersonJobsEntity with Scene
  type ColleagueFace = Colleague with PersonPublicEntity with PersonConnectionsEntity with PersonJobsEntity with Scene

}

import Model._

trait Scene {

  def endUserKernel: &![EndUserFace]

  def colleaguesKernels: Iterable[&![ColleagueFace]]

  lazy val endUser: EndUserFace = *(endUserKernel).~

  lazy val colleagues: Iterable[ColleagueFace] = colleaguesKernels.map(*(_).~)

}

// Mapping a network of persons to a network of colleagues

class NetworkMapper(val sourceNetwork: Map[String, personMorphModel.Kernel]) extends AbstractNetworkMapper {
  override val sourceMorphModel = personMorphModel
  override val targetMorphModel = roleMorphModel
  override type Subject = EndUserFace

  override def mapKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
    val srcKernelRef: &[RoleMorphType] = srcKernel.~
    *(srcKernelRef, single[EndUser], single[Colleague], external[Scene](new SceneImpl(srcKernel.~)))
  }

  override protected def makeSelf(selfKernelHolder: TargetKernelHolder): &![Subject] = {
    selfKernelHolder.kernel
  }

  private class SceneImpl(self: PersonPublicEntity with PersonJobsEntity) extends Scene {

    lazy val endUserKernel: &![EndUserFace] = targetNetwork(self.nick)

    lazy val colleaguesKernels: Iterable[&![ColleagueFace]] = for (pk <- targetNetwork.values if self.isColleague(pk.~))
      yield {
        val cref: &![ColleagueFace] = pk; cref
      }

  }

  def signIn(userNick: String): Either[EndUserFace, String] = login(userNick) match {
    case Right(errMsg) => Right(errMsg)
    case Left(userRef) =>
      val user = *(userRef).~
      user.onLogin()
      Left(user)
  }

  def signOut(user: EndUserFace): Unit = {
    user.onLogout()
  }
}

// Application

object Colleagues {

  val sampleNetwork = new NetworkMapper(PersonSample.personsAsMap)

  def main(args: Array[String]) {
    if (args.isEmpty) {
      sys.error("No user nick specified")
    }

    val person = PersonSample.personsAsMap(args(0))
    person.~.remorph()
    println(s"${person.~.nick} is online: ${person.~.isOnline}")

    sampleNetwork.signIn(args(0)) match {
      case Right(errMsg) => println(errMsg)
      case Left(user) =>

        println(s"${user.nick}: ${user.colleagues.map(_.nick)}")

        person.~.remorph()
        println(s"${person.~.nick} is online: ${person.~.isOnline}")

        sampleNetwork.signOut(user)

        person.~.remorph()
        println(s"${person.~.nick} is online: ${person.~.isOnline}")
    }

  }

}