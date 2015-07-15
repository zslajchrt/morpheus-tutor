package org.cloudio.morpheus.dci.socnet.networks.friends

import org.cloudio.morpheus.dci.socnet.objects.{PersonConnections, PersonPublic, PersonPrivate}
import org.cloudio.morpheus.dci.socnet.networks._
import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 14/07/15.
 */

@dimension
trait PhoneHolder {

  def myPhone(): Option[String]

}

@fragment
trait FriendPhoneHolder extends PhoneHolder {
  this: Friend with PersonPrivate =>

  override def myPhone(): Option[String] = Some(phone)
}

@fragment
trait NonFriendPhoneHolder extends PhoneHolder {
  this: NonFriend =>

  override def myPhone(): Option[String] = None
}

@fragment
trait Requester {

  this: SelfParty with PartyScene with PersonPrivate =>

  lazy val friendlyHolders: List[FriendPhoneHolder] = {
    friendKernels.map(fk => {
      val frk: &[$[FriendPhoneHolder]] = *(fk)
      *(frk, single[FriendPhoneHolder]).~
    })
  }

  lazy val nonFriendlyHolders: List[NonFriendPhoneHolder] = {
    nonFriendKernels.map(fk => {
      val frk: &[$[NonFriendPhoneHolder]] = *(fk)
      *(frk, single[NonFriendPhoneHolder]).~
    })
  }

  def requestPhoneOf(phoneOwnerNick: String): Option[String] = {
//    for (phoneOwner <- findPartyByNick(phoneOwnerNick);
//         phone <- traverseContactsForPhone(phoneOwner, this /* this requester IS a PersonBasic */, Nil)) yield phone
    println(friendlyHolders.map(_.myPhone()))
    None
  }

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

}

//class NonFriendPhoneRequest(friendship: FriendshipNetwork) {
//
//  class RequestKernel(requestKernel: partyMorphModel.Kernel) {
//
//    lazy val kernel: requestMorphModel.Kernel = {
//      val personKernelRef: &[RequestMorphType] = requestKernel.~
//      *(personKernelRef, single[FriendPhoneHolder], single[NonFriendPhoneHolder], single[Requester])
//    }
//
//  }
//
//  val requestKernels: Map[String, RequestKernel] = friendship.partyKernels.mapValues(pk => new RequestKernel(pk.kernel)).view.force
//
//
//}


class NonFriendPhoneRequestNetworkMapper(val myNetwork: FriendshipNetworkMapper) extends AbstractNetworkMapper {
  override val sourceNetwork = myNetwork.targetNetwork
  override val sourceMorphModel = myNetwork.targetMorphModel

  type RequestMorphType = $[FriendPhoneHolder] or $[NonFriendPhoneHolder] or $[Requester]

  override val targetMorphModel = parse[RequestMorphType](false, true)

  override type SelfType = Requester

  override def createTargetKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
    val personKernelRef: &[RequestMorphType] = srcKernel.~
    *(personKernelRef, single[FriendPhoneHolder], single[NonFriendPhoneHolder], single[Requester])
  }

  override protected def nodeSelf(nodeKernelHolder: TargetKernelHolder): SelfType =
    asMorphOf[SelfType](nodeKernelHolder.kernel)
}

object NonFriendPhoneRequest {

  def main(args: Array[String]) {
    val phReqNetwork = new NonFriendPhoneRequestNetworkMapper(Friendship.sampleNetwork)
    val pr1: Requester = phReqNetwork.login("person1").get
    pr1.requestPhoneOf("person2")

  }

  //  type RequestMorphType =
  //  ($[FriendPhoneHolder] with Friend with PersonPrivate) or
  //    ($[NonFriendPhoneHolder] with NonFriend) or
  //    $[Requester]
  //  val requestMorphModel = parse[RequestMorphType](false, true)

}

