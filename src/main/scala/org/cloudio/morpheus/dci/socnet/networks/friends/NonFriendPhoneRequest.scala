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

  def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublic with PersonConnections, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String]

}

@fragment
trait Requester {

  this: SelfParty with PhoneHolderScene with PersonPublic with PersonConnections with PersonPrivate =>

  def requestPhoneOf(phoneOwnerNick: String): Option[String] = {
    for (phoneOwner <- allMyContactsAsPhoneHolders.find(_.nick == phoneOwnerNick);
         phone <- traverseContactsForPhone(phoneOwner, this, Nil)) yield phone
  }

}

@fragment
trait NonFriendPhoneHolder extends PhoneHolder {
  this: NonFriend with PersonPublic with PersonConnections with PhoneHolderScene =>

  override def myPhone(): Option[String] = None

  override def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublic with PersonConnections, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (isTrusted(path.head.nick))
      traverseContactsForPhone(phoneHolder, onBehalfOf, path)
    else
      None
  }
}

@fragment
trait FriendPhoneHolder extends PhoneHolder {
  this: Friend with PersonPublic with PersonPrivate with PersonConnections with PhoneHolderScene =>

  override def myPhone(): Option[String] = Some(phone)

  override def mediatePhoneRequest(phoneHolder: PhoneHolder with PersonPublic with PersonConnections, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (phoneHolder.nick == this.nick) {
      if (examineRequester(onBehalfOf)) {
        Some(phone)
      } else {
        None
      }
    } else {
      traverseContactsForPhone(phoneHolder, onBehalfOf, path)
    }
  }

  protected def examineRequester(requester: PersonPublic): Boolean = {
    true
  }

}

@fragment
trait PhoneHolderScene {
  this: PartyScene with PersonPublic =>

  lazy val myFriendsAsPhoneHolders: List[FriendPhoneHolder with PersonPublic with PersonConnections] = {
    friendKernels.map(fk => {
      val frk: &[$[FriendPhoneHolder] with $[PhoneHolderScene] with PersonPublic with PersonConnections] = *(fk)
      *(frk, single[FriendPhoneHolder], single[PhoneHolderScene]).~
    })
  }

  lazy val myNonFriendsAsPhoneHolders: List[NonFriendPhoneHolder with PersonPublic with PersonConnections] = {
    nonFriendKernels.map(fk => {
      val frk: &[$[NonFriendPhoneHolder] with PersonPublic with PersonConnections] = *(fk)
      *(frk, single[NonFriendPhoneHolder]).~
    })
  }

  lazy val allMyContactsAsPhoneHolders = myFriendsAsPhoneHolders ::: myNonFriendsAsPhoneHolders

  protected def traverseContactsForPhone(phoneHolder: PhoneHolder with PersonPublic with PersonConnections, onBehalfOf: PersonPublic, path: List[PersonPublic]): Option[String] = {
    if (path.exists(_.nick == this.nick)) {
      None
    } else {
      var maybePhone: Option[String] = None
      val phoneHolderContactIterator = allMyContactsAsPhoneHolders.iterator
      while (phoneHolderContactIterator.hasNext && !maybePhone.isDefined) {
        maybePhone = phoneHolderContactIterator.next().mediatePhoneRequest(phoneHolder, onBehalfOf, this :: path)
      }
      maybePhone
    }

  }

}

object NonFriendPhoneRequest {

  def main(args: Array[String]) {
    val requester = Friendship.sampleNetwork.login("person1") match {
      case None => sys.error(s"No such person")
      case Some(personRef) =>
        val prRef: &[$[Requester] with $[PhoneHolderScene]] = *(personRef)
        *(prRef, single[Requester], single[PhoneHolderScene]).~
    }
    println(requester.requestPhoneOf("person2"))
  }

}
