package org.cloudio.morpheus.dci.socnet.cases.phoneRequest

import org.cloudio.morpheus.dci.socnet.objects.PersonPrivate

import org.morpheus._
import org.morpheus.Morpheus._

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
  def requestPhoneOf(nick: String): Option[String] = {
    None // todo
  }
}

object NonFriendPhoneRequest {

//  type RequestMorphType =
//  ($[FriendPhoneHolder] with Friend with PersonPrivate) or
//    ($[NonFriendPhoneHolder] with NonFriend) or
//    $[Requester]
//  val requestMorphModel = parse[RequestMorphType](false, true)

}

import Roles._
import NonFriendPhoneRequest._

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


class NonFriendPhoneRequestNetworkMapper(val myNetwork: MyNetworkMapper) extends AbstractNetworkMapper {
  override val sourceNetwork = myNetwork.targetNetwork
  override val sourceMorphModel = myNetwork.targetMorphModel

  type RequestMorphType =
  ($[FriendPhoneHolder] with Friend with PersonPrivate) or
    ($[NonFriendPhoneHolder] with NonFriend) or
    $[Requester]

  override val targetMorphModel = parse[RequestMorphType](false, true)

  override type SelfType = Requester

  override def createTargetKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
    val personKernelRef: &[RequestMorphType] = srcKernel.~
    *(personKernelRef, single[FriendPhoneHolder], single[NonFriendPhoneHolder], single[Requester])
  }

  override def nodeSelf(nodeKernelHolder: TargetKernelHolder): SelfType = asMorphOf[SelfType](nodeKernelHolder.kernel)

}
