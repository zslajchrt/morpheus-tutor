package org.cloudio.morpheus.dci.socnet.cases.phoneRequest

import org.cloudio.morpheus.dci.socnet.objects._
import org.cloudio.morpheus.dci.socnet.objects.Person._

import org.morpheus._
import org.morpheus.Morpheus._


/**
 *
 * Created by zslajchrt on 29/06/15.
 */


trait PartyScene {
  def friends: List[PersonPublic with FaceCommon with PersonPrivate with Friend]

  def nonFriends: List[PersonPublic with FaceCommon with NonFriend]

  def allContacts: List[PersonPublic with FaceCommon with PersonFace] = friends ::: nonFriends
}

//@fragment
//protected trait PartyTools {
//  // This role is a projection of the Person entity.
//  // This role's scene consists of faces of the contact parties
//  this: PersonPublic with PartyScene =>
//
//  // a helper for the traversal of the graph of contacts
//  protected def traverseContactsForPhone(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
//    if (path.exists(_.nick == this.nick)) {
//      None
//    } else {
//      var maybePhone: Option[String] = None
//      val contactIterator = contacts.iterator
//      while (contactIterator.hasNext && !maybePhone.isDefined) {
//        maybePhone = contactIterator.next().phoneRequest(target, onBehalfOf, this :: path)
//      }
//      maybePhone
//    }
//
//  }
//
//}

//@dimension
//protected trait Role {
//
//}
//
//// role
//// The end user is identified with this role. It corresponds to the UI interface of the end user.
//// The operations - i.e. askForPhone only - and the scene will be reflected on the end user's terminal.
//@fragment
//protected trait Requester extends Role {
//  this: PersonPublic with PersonPrivate with PartyTools with PartyScene =>
//
//  def askForPhone(phoneOwnerNick: String): Option[String] = {
//    for (target <- findByNick(phoneOwnerNick);
//         phone <- traverseContactsForPhone(target, this /* this requester IS a PersonBasic */, Nil)) yield phone
//  }
//
//  private def findByNick(nick: String): Option[PersonPublic with PersonFace] = contacts.find(_.nick == nick)
//
////  def start(): Unit = {
////    client.receive({
////      case "" =>
////        val requestedPhone = askForPhone("")
////        client.send(Map("requestedPhone" -> requestedPhone))
////    })
////    client.send(Map("nick" -> nick, "phone" -> phone))
////  }
//
//}

//@fragment
//protected trait Requested extends Role {
//
//}
//
//@fragment
//protected trait Other extends Role {
//
//}

// an abstract face of the party
@dimension
trait PersonFace {
  //  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String]

  def faceName: String

}

// the friendly face of the party
@fragment
trait Self extends PersonFace {
  this: PersonPublic with
    PersonPrivate with
    PartyScene =>

  override def faceName: String = "Self"
}

@fragment
trait Friend extends PersonFace {
  this: PersonPublic with
    PersonPrivate with
    PartyScene =>

  //  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
  //    if (target.nick == this.nick) {
  //      if (examineRequester(onBehalfOf)) {
  //        Some(phone)
  //      } else {
  //        None
  //      }
  //    } else {
  //      traverseContactsForPhone(target, onBehalfOf, path)
  //    }
  //  }
  //
  //  private def examineRequester(requester: PersonPublic): Boolean = {
  //    true
  //  }
  override def faceName: String = "Friend"
}

// just an associate
@fragment
trait NonFriend extends PersonFace {
  this: PersonConnections =>

  //  def phoneRequest(target: PersonPublic, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
  //    if (isTrusted(path.head.nick))
  //      traverseContactsForPhone(target, onBehalfOf, path)
  //    else
  //      None
  //  }
  override def faceName: String = "Non-friend"
}

@fragment
trait FaceCommon {
  this: PersonFace =>

  def presentation = {
    s"This is face ${faceName}"
  }

}

// Assembling


// The Party role's valid forms expressed by means of its morph type
object Roles {
  //  type PartyMorphType = PersonPublic with PersonConnections with
  //    (($[Requester] with PersonPrivate with $[RequesterScene]) or $[Requested] or $[Other]) with
  //    (($[Friend] with PersonPrivate with $[PartyScene]) or
  //      ($[Contact] with $[PartyScene])) with
  //    $[PartyTools]

  type PartyMorphType = PersonPublic with PersonConnections with $[FaceCommon] with
    (($[Self] with PersonPrivate) or
      ($[Friend] with PersonPrivate) or
      $[NonFriend]) with
    $[PartyScene]

  val partyMorphModel = parse[PartyMorphType](true, true)
}

abstract class AbstractNetworkMapper {
  val sourceMorphModel: MorphModel[_]
  val targetMorphModel: MorphModel[_]
  val sourceNetwork: Map[String, sourceMorphModel.Kernel]
  type SelfType

  class TargetKernelHolder(srcKernel: sourceMorphModel.Kernel) {
    lazy val kernel: targetMorphModel.Kernel = createTargetKernel(srcKernel)
  }

  def nodeSelf(nodeKernelHolder: TargetKernelHolder): SelfType

  lazy val targetKernelHolders: Map[String, TargetKernelHolder] = sourceNetwork.map(srcKernEntry => (srcKernEntry._1, new TargetKernelHolder(srcKernEntry._2)))

  // a view
  lazy val targetNetwork: Map[String, targetMorphModel.Kernel] = targetKernelHolders.mapValues(_.kernel).view.force

  def createTargetKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel

  def login(nodeId: String): Option[SelfType] = {
    // todo: authentication and authorization
    for (kh <- targetKernelHolders.get(nodeId)) yield nodeSelf(kh)
  }

}

import Roles._

class MyNetworkMapper(val sourceNetwork: Map[String, personMorphModel.Kernel]) extends AbstractNetworkMapper {
  override val sourceMorphModel = personMorphModel
  type TargetMorphType = PartyMorphType
  override val targetMorphModel = parse[TargetMorphType](true, true)
  override type SelfType = Self with FaceCommon with PersonPublic with PersonPrivate with PartyScene

  override def createTargetKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
    val srcKernelRef: &[PartyMorphType] = srcKernel.~
    *(srcKernelRef, single[FaceCommon], single[Self], single[Friend], single[NonFriend], external[PartyScene](new PartySceneImpl(srcKernel)))
  }

  override def nodeSelf(nodeKernelHolder: TargetKernelHolder): SelfType = asMorphOf[SelfType](nodeKernelHolder.kernel)

  private class PartySceneImpl(person: sourceMorphModel.Kernel) extends PartyScene {

    def isFriend(p1: personMorphModel.Kernel, p2: partyMorphModel.Kernel): Boolean = {
      p1.~.isTrusted(p2.~.nick) && p2.~.isTrusted(p1.~.nick)
    }

    lazy val friends =
      for (p <- person.~.connections;
           pp <- targetNetwork.get(p.person.nick) if isFriend(person, pp))
        yield asMorphOf[PersonPublic with FaceCommon with PersonPrivate with Friend](pp)

    lazy val nonFriends =
      for (p <- person.~.connections;
           pp <- targetNetwork.get(p.person.nick) if !isFriend(person, pp))
        yield asMorphOf[PersonPublic with FaceCommon with NonFriend](pp)

  }

}

//class FriendshipNetwork(network: List[personMorphModel.Kernel]) {
//
//  class PartyKernel(personKernel: personMorphModel.Kernel) {
//    val nick = personKernel.~.nick
//
//    lazy val kernel: partyMorphModel.Kernel = {
//      println(s"Making kernel for $nick")
//      val personKernelRef: &[PartyMorphType] = personKernel.~ // using ~ ensures that the subjective morph is connected with the original morph
//      //val personWithCons = asMorphOf[PersonPublic with PersonConnections](personKernel)
//      *(personKernelRef, single[FaceCommon], single[Self], single[Friend], single[NonFriend], external[PartyScene](new PartySceneImpl(personKernel))
//      )
//    }
//
//  }
//
//  def isFriend(p1: personMorphModel.Kernel, p2: partyMorphModel.Kernel): Boolean = {
//    p1.~.isTrusted(p2.~.nick) && p2.~.isTrusted(p1.~.nick)
//  }
//
//  val partyKernels: Map[String, PartyKernel] = network.map(new PartyKernel(_)).map(p => (p.nick, p)).toMap
//
//  private class PartySceneImpl(person: personMorphModel.Kernel) extends PartyScene {
//
//    override def friends = for (p <- person.~.connections; pp <- partyKernels.get(p.person.nick) if isFriend(person, pp.kernel)) yield asMorphOf[PersonPublic with FaceCommon with PersonPrivate with Friend](pp.kernel)
//
//    override def nonFriends = for (p <- person.~.connections; pp <- partyKernels.get(p.person.nick) if !isFriend(person, pp.kernel)) yield asMorphOf[PersonPublic with FaceCommon with NonFriend](pp.kernel)
//
//  }
//
//  def login(userNick: String): Option[Self with FaceCommon with PersonPublic with PersonPrivate with PartyScene] = {
//    // todo: authentication and authorization
//    for (k <- partyKernels.get(userNick)) yield asMorphOf[Self with FaceCommon with PersonPublic with PersonPrivate with PartyScene](k.kernel)
//  }
//
//  //  def apply(requesterNick: String, phoneOwnerNick: String) = {
//  //    partyKernels.get(requesterNick) match {
//  //      case None => sys.error(s"No party with nick $requesterNick found")
//  //      case Some(requesterKernel) =>
//  //        val requester = asMorphOf[PersonPublic with Requester](requesterKernel)
//  //        requester.askForPhone(phoneOwnerNick)
//  //    }
//  //  }
//
//}

// Application

object App {

  def main(args: Array[String]) {

    def newPerson(id: Int): (String, personMorphModel.Kernel) = {
      val nick = s"person$id"
      implicit val personFrag = single[SomeonePublic, PersonPublic](PersonPublicInit(nick, s"Pepa$id", s"Novak$id", s"pepa$id@depo.cz"))
      implicit val personPrivateFrag = single[SomeonePrivate, PersonPrivate](PersonPrivateInit(s"+420 00$id", Address("Prague", s"Moskevska $id", "Czech Rep")))
      val kernel = singleton(personMorphModel, rootStrategy(personMorphModel))
      (nick, kernel)
    }


    val p1 = newPerson(1)
    val p2 = newPerson(2)
    val p3 = newPerson(3)
    val p4 = newPerson(4)

    //    P1 -> (P2, P3*) (* means 'trusted')
    //    P2 -> (P3*)
    //    P3 -> (P1*, P2*)

    implicit def personKernelToPersonWithContacts(personKernel: personMorphModel.Kernel): PersonPublic with PersonConnections = {
      asMorphOf[PersonPublic with PersonConnections](personKernel)
    }

    p1._2.addConnection(Connection(p2._2, trusted = true))
    p1._2.addConnection(Connection(p4._2, trusted = false))
    p2._2.addConnection(Connection(p1._2, trusted = true))
    p2._2.addConnection(Connection(p3._2, trusted = true))
    p3._2.addConnection(Connection(p2._2, trusted = false))
    p3._2.addConnection(Connection(p4._2, trusted = true))
    p4._2.addConnection(Connection(p3._2, trusted = true))

    val myNetwork = new MyNetworkMapper(Map(p1, p2, p3, p4))

    val r1: myNetwork.SelfType = myNetwork.login("person1").get
    println(s"${r1.allContacts.map(_.presentation)}")

    val r2: myNetwork.SelfType = myNetwork.login("person2").get
    println(s"${r2.allContacts.map(_.presentation)}")

    val r3: myNetwork.SelfType = myNetwork.login("person3").get
    println(s"${r3.allContacts.map(_.presentation)}")

    val r4: myNetwork.SelfType = myNetwork.login("person4").get
    println(s"${r4.allContacts.map(_.presentation)}")

    val phReqNetwork = new NonFriendPhoneRequestNetworkMapper(myNetwork)
    val pr1: Requester = phReqNetwork.login("person1").get
    pr1.requestPhoneOf("person2")

  }

}