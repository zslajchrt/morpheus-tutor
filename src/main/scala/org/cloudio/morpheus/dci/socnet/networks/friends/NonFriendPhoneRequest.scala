//package org.cloudio.morpheus.dci.socnet.networks.friends
//
//import org.cloudio.morpheus.dci.socnet.objects.{PersonConnectionsEntity, PersonPublicCommon, PersonPrivateV2_0Entity}
//import org.cloudio.morpheus.dci.socnet.networks._
//import org.morpheus.Morpheus._
//import org.morpheus._
//
///**
// * Created by zslajchrt on 14/07/15.
// */
//
//// Behavioral role traits
//
//// The base of all role traits
//@dimension
//trait PhoneHolder {
//
//  def myPhone(): Option[String]
//
//  def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublicCommon with PersonConnectionsEntity, onBehalfOf: PersonPublicCommon, path: List[PersonPublicCommon]): Option[String]
//
//}
//
//// This trait is bound to EndUser
//@fragment
//trait Requester {
//
//  this: EndUser with PhoneHolderScene with PersonPublicCommon with PersonConnectionsEntity with PersonPrivateV2_0Entity =>
//
//  def requestPhoneOf(phoneOwnerNick: String): Option[String] = {
//    for (phoneOwner <- allMyContactsAsPhoneHolders.find(_.nick == phoneOwnerNick);
//         phone <- traverseContactsForPhone(phoneOwner, this, Nil)) yield phone
//  }
//
//}
//
//// This trait is bound to NonFriend
//@fragment
//trait NonFriendPhoneHolder extends PhoneHolder {
//  this: NonFriend with PersonPublicCommon with PersonConnectionsEntity with PhoneHolderScene =>
//
//  override def myPhone(): Option[String] = None
//
//  override def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublicCommon with PersonConnectionsEntity, onBehalfOf: PersonPublicCommon, path: List[PersonPublicCommon]): Option[String] = {
//    if (isTrusted(path.head.nick))
//      traverseContactsForPhone(phoneHolder, onBehalfOf, path)
//    else
//      None
//  }
//}
//
//// This trait is bound to Friend
//@fragment
//trait FriendPhoneHolder extends PhoneHolder {
//  this: Friend with PersonPublicCommon with PersonPrivateV2_0Entity with PersonConnectionsEntity with PhoneHolderScene =>
//
//  override def myPhone(): Option[String] = Some(personPrivate.phone)
//
//  override def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublicCommon with PersonConnectionsEntity, onBehalfOf: PersonPublicCommon, path: List[PersonPublicCommon]): Option[String] = {
//    if (phoneHolder.nick == this.nick) {
//      if (examineRequester(onBehalfOf)) {
//        Some(personPrivate.phone)
//      } else {
//        None
//      }
//    } else {
//      traverseContactsForPhone(phoneHolder, onBehalfOf, path)
//    }
//  }
//
//  protected def examineRequester(requester: PersonPublicCommon): Boolean = {
//    true
//  }
//
//}
//
//// This scene is a view that transforms the friendship roles to phone holders
//@fragment
//trait PhoneHolderScene {
//  this: PartyScene with PersonPublicCommon =>
//
//  lazy val myFriendsAsPhoneHolders: List[FriendPhoneHolder with PersonPublicCommon with PersonConnectionsEntity] = {
//    friendKernels.map(fk => {
//      val frk: &[$[FriendPhoneHolder] with $[PhoneHolderScene] with PersonPublicCommon with PersonConnectionsEntity] = *(fk)
//      *(frk, single[FriendPhoneHolder], single[PhoneHolderScene]).~
//    })
//  }
//
//  lazy val myNonFriendsAsPhoneHolders: List[NonFriendPhoneHolder with PersonPublicCommon with PersonConnectionsEntity] = {
//    nonFriendKernels.map(fk => {
//      val frk: &[$[NonFriendPhoneHolder] with $[PhoneHolderScene] with PersonPublicCommon with PersonConnectionsEntity] = *(fk)
//      *(frk, single[NonFriendPhoneHolder], single[PhoneHolderScene]).~
//    })
//  }
//
//  lazy val allMyContactsAsPhoneHolders = myFriendsAsPhoneHolders ::: myNonFriendsAsPhoneHolders
//
//  protected def traverseContactsForPhone(phoneHolder: PhoneHolder with PersonPublicCommon with PersonConnectionsEntity, onBehalfOf: PersonPublicCommon, path: List[PersonPublicCommon]): Option[String] = {
//    if (path.exists(_.nick == this.nick)) {
//      None
//    } else {
//      var maybePhone: Option[String] = None
//      val phoneHolderContactIterator = allMyContactsAsPhoneHolders.iterator
//      while (phoneHolderContactIterator.hasNext && !maybePhone.isDefined) {
//        maybePhone = phoneHolderContactIterator.next().mediatePhoneRequest(phoneHolder, onBehalfOf, this :: path)
//      }
//      maybePhone
//    }
//
//  }
//
//}
//
//// Application
//
//object NonFriendPhoneRequest {
//
//  def main(args: Array[String]) {
//    val requester = Friendship.sampleNetwork.login("joe1") match {
//      case Right(errMsg) => sys.error(errMsg)
//      case Left(personRef) =>
//        val prRef: &[$[Requester] with $[PhoneHolderScene]] = *(personRef)
//        *(prRef, single[Requester], single[PhoneHolderScene]).~
//    }
//    println(requester.requestPhoneOf("joe2"))
//  }
//
//}
