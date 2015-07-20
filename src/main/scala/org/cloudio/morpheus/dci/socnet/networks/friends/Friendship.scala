package org.cloudio.morpheus.dci.socnet.networks.friends

import org.cloudio.morpheus.dci.socnet.objects.Person._
import org.cloudio.morpheus.dci.socnet.objects._
import org.cloudio.morpheus.dci.socnet.networks._
import org.morpheus.Morpheus._
import org.morpheus._


/**
 *
 * Created by zslajchrt on 29/06/15.
 */

// an abstract face of the party
@dimension
trait PartyFace {
}

// The friendly face of the party.
@fragment
trait SelfParty extends PartyFace {
}

@fragment
trait Friend extends PartyFace {
}

// just an associate
@fragment
trait NonFriend extends PartyFace {
}

object FriendshipNetworkTypes {

  // Morph type
  type PartyMorphType = PersonPublic with PersonConnections with $[PartyScene] with
    (($[SelfParty] with PersonPrivate) or
      ($[Friend] with PersonPrivate) or
      $[NonFriend])

  // Morph model
  val partyMorphModel = parse[PartyMorphType](false, true)

  // Faces
  type Subject = SelfParty with PersonPublic with PersonPrivate with PersonConnections with PartyScene
  type FriendFace = Friend with PersonPublic with PersonPrivate with PersonConnections with PartyScene
  type NonFriendFace = NonFriend with PersonPublic with PersonConnections with PartyScene

}

import FriendshipNetworkTypes._

trait PartyScene {

  def subjectKernel: &![Subject]

  def friendKernels: List[&![FriendFace]]

  def nonFriendKernels: List[&![NonFriendFace]]

  lazy val subject: Subject = *(subjectKernel).~

  lazy val friends: List[FriendFace] = friendKernels.map(fk => {
    *(fk).~
  })

  lazy val nonFriends: List[NonFriendFace] = nonFriendKernels.map(nfk => {
    *(nfk).~
  })
}

// Assembling

class FriendshipNetworkMapper(val sourceNetwork: Map[String, personMorphModel.Kernel]) extends AbstractNetworkMapper {
  override val sourceMorphModel = personMorphModel
  type TargetMorphType = PartyMorphType
  override val targetMorphModel = parse[TargetMorphType](false, true)
  override type SelfType = Subject

  override def mapKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
    val srcKernelRef: &[PartyMorphType] = srcKernel.~
    *(srcKernelRef, single[SelfParty], single[Friend], single[NonFriend], external[PartyScene](new PartySceneImpl(srcKernel.~)))
  }

  override protected def nodeSelf(nodeKernelHolder: TargetKernelHolder): &![SelfType] = {
    nodeKernelHolder.kernel
  }

  private class PartySceneImpl(person: PersonPublic with PersonConnections) extends PartyScene {

    def findPerson(nick: String): Option[PersonPublic with PersonConnections] = {
      for (pk <- targetNetwork.get(nick)) yield pk.~
    }

    def morphFriendFace(nick: String): &![FriendFace] = {
      targetNetwork(nick)
    }

    def morphNonFriendFace(nick: String): &![NonFriendFace] = {
      targetNetwork(nick)
    }

    lazy val subjectKernel: &![Subject] = targetNetwork(person.nick)

    lazy val friendKernels =
      for (p <- subject.connections;
           pp <- findPerson(p.personNick) if isFriend(subject, pp))
        yield morphFriendFace(pp.nick)

    lazy val nonFriendKernels =
      for (p <- subject.connections;
           pp <- findPerson(p.personNick) if !isFriend(subject, pp))
        yield morphNonFriendFace(pp.nick)

    lazy val allContacts = friendKernels ::: nonFriendKernels

    def isFriend(p1: PersonPublic with PersonConnections, p2: PersonPublic with PersonConnections): Boolean = {
      p1.isTrusted(p2.nick) && p2.isTrusted(p1.nick)
    }

  }

}

// Application

object Friendship {

  def newPerson(id: Int): (String, personMorphModel.Kernel) = {
    val nick = s"person$id"
    implicit val personFrag = single[SomeonePublic, PersonPublic](PersonPublicInit(nick, s"Pepa$id", s"Novak$id", s"pepa$id@depo.cz"))
    implicit val personPrivateFrag = single[SomeonePrivate, PersonPrivate](PersonPrivateInit(s"+420 00$id", Address("Prague", s"Moskevska $id", "Czech Rep")))
    val kernel = singleton(personMorphModel, rootStrategy(personMorphModel))
    (nick, kernel)
  }

  implicit def personKernelToPersonWithContacts(personKernel: personMorphModel.Kernel): PersonPublic with PersonConnections = {
    asMorphOf[PersonPublic with PersonConnections](personKernel)
  }

  val p1 = newPerson(1)
  val p2 = newPerson(2)
  val p3 = newPerson(3)
  val p4 = newPerson(4)

  p1._2.addConnection(Connection(p2._1, trusted = true))
  p1._2.addConnection(Connection(p4._1, trusted = false))
  p2._2.addConnection(Connection(p1._1, trusted = true))
  p2._2.addConnection(Connection(p3._1, trusted = true))
  p3._2.addConnection(Connection(p2._1, trusted = false))
  p3._2.addConnection(Connection(p4._1, trusted = true))
  p4._2.addConnection(Connection(p3._1, trusted = true))

  val sampleNetwork = new FriendshipNetworkMapper(Map(p1, p2, p3, p4))

   def main(args: Array[String]) {

    //    P1 -> (P2, P3*) (* means 'trusted')
    //    P2 -> (P3*)
    //    P3 -> (P1*, P2*)

     val r1Ref = sampleNetwork.login("person1").get
     val r1 = *(r1Ref).~
     println(s"${r1.nick}: ${r1.friends.map(_.nick)}")

     val r2Ref = sampleNetwork.login("person2").get
     val r2 = *(r2Ref).~
     println(s"${r2.nick}: ${r2.friends.map(_.nick)}")

   }

}