//package org.cloudio.morpheus.dci.socnet.networks.friends
//
//import org.cloudio.morpheus.dci.socnet.objects.Person._
//import org.cloudio.morpheus.dci.socnet.objects._
//import org.cloudio.morpheus.dci.socnet.networks._
//import org.morpheus.Morpheus._
//import org.morpheus._
//
//
///**
// *
// * Created by zslajchrt on 29/06/15.
// */
//
//// Roles
//
//@fragment
//trait EndUser {
//}
//
//@fragment
//trait Friend {
//}
//
//@fragment
//trait NonFriend {
//}
//
//// Roles Morph Model
//
//object Model {
//
//  // Morph type
//  type PartyMorphType = PersonPublicCommon with PersonConnectionsEntity with $[PartyScene] with
//    (($[EndUser] with PersonPrivateV2_0Entity) or
//      ($[Friend] with PersonPrivateV2_0Entity) or
//      $[NonFriend])
//
//  // Morph model
//  val partyMorphModel = parse[PartyMorphType](false, true)
//
//  // Faces
//  type EndUserFace = EndUser with PersonPublicCommon with PersonPrivateV2_0Entity with PersonConnectionsEntity with PartyScene
//  type FriendFace = Friend with PersonPublicCommon with PersonPrivateV2_0Entity with PersonConnectionsEntity with PartyScene
//  type NonFriendFace = NonFriend with PersonPublicCommon with PersonConnectionsEntity with PartyScene
//
//}
//
//import Model._
//
//trait PartyScene {
//
//  def endUserKernel: &![EndUserFace]
//
//  def friendKernels: List[&![FriendFace]]
//
//  def nonFriendKernels: List[&![NonFriendFace]]
//
//  lazy val subject: EndUserFace = *(endUserKernel).~
//
//  lazy val friends: List[FriendFace] = friendKernels.map(*(_).~)
//
//  lazy val nonFriends: List[NonFriendFace] = nonFriendKernels.map(*(_).~)
//}
//
//// Mapping a network of persons to a network of friends
//
//class NetworkMapper(val sourceNetwork: Map[String, personMorphModel.Kernel]) extends AbstractNetworkMapper {
//  override val sourceMorphModel = personMorphModel
//  override val targetMorphModel = partyMorphModel
//  override type Subject = EndUserFace
//
//  override def mapKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel = {
//    val srcKernelRef: &[PartyMorphType] = srcKernel.~
//    *(srcKernelRef, single[EndUser], single[Friend], single[NonFriend], external[PartyScene](new PartySceneImpl(srcKernel.~)))
//  }
//
//  override protected def makeSelf(selfKernelHolder: TargetKernelHolder): &![Subject] = {
//    selfKernelHolder.kernel
//  }
//
//  private class PartySceneImpl(person: PersonPublicCommon with PersonConnectionsEntity) extends PartyScene {
//
//    def findPerson(nick: String): Option[PersonPublicCommon with PersonConnectionsEntity] = {
//      for (pk <- targetNetwork.get(nick)) yield pk.~
//    }
//
//    def morphFriendFace(nick: String): &![FriendFace] = {
//      targetNetwork(nick)
//    }
//
//    def morphNonFriendFace(nick: String): &![NonFriendFace] = {
//      targetNetwork(nick)
//    }
//
//    lazy val endUserKernel: &![Subject] = targetNetwork(person.nick)
//
//    lazy val friendKernels =
//      for (p <- subject.allConnections;
//           pp <- findPerson(p.personNick) if isFriend(subject, pp))
//        yield morphFriendFace(pp.nick)
//
//    lazy val nonFriendKernels =
//      for (p <- subject.allConnections;
//           pp <- findPerson(p.personNick) if !isFriend(subject, pp))
//        yield morphNonFriendFace(pp.nick)
//
//    lazy val allContacts = friendKernels ::: nonFriendKernels
//
//    def isFriend(p1: PersonPublicCommon with PersonConnectionsEntity, p2: PersonPublicCommon with PersonConnectionsEntity): Boolean = {
//      p1.isTrusted(p2.nick) && p2.isTrusted(p1.nick)
//    }
//
//  }
//
//}
//
//// Application
//
//object Friendship {
//
//  val sampleNetwork = new NetworkMapper(PersonSample.personsAsMap)
//
//   def main(args: Array[String]) {
//
//     val r1Ref = sampleNetwork.login("joe1").left.get
//     val r1 = *(r1Ref).~
//     println(s"${r1.nick}: ${r1.friends.map(_.nick)}")
//
//     val r2Ref = sampleNetwork.login("joe2").left.get
//     val r2 = *(r2Ref).~
//     println(s"${r2.nick}: ${r2.friends.map(_.nick)}")
//
//     val r3Ref = sampleNetwork.login("joe3").left.get
//     val r3 = *(r3Ref).~
//     println(s"${r3.nick}: ${r3.friends.map(_.nick)}")
//
//     val r4Ref = sampleNetwork.login("joe4").left.get
//     val r4 = *(r4Ref).~
//     println(s"${r4.nick}: ${r4.friends.map(_.nick)}")
//   }
//
//}