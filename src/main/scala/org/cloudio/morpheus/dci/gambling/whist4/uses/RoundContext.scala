//package org.cloudio.morpheus.dci.gambling.whist4.uses
//
//import org.morpheus._
//import org.morpheus.Morpheus._
//import org.cloudio.morpheus.dci.gambling.whist4.data._
//import org.cloudio.morpheus.dci.gambling.objects._
//
///**
// * Created by zslajchrt on 30/07/15.
// */
//object RoundContext {
//
//  type PlayerMorphType = Person with Player with PlayerScene with
//    (LivePlayerLogic or RobotPlayerLogic)
//    with
//    (OpponentFace or PartnerFace) with
//    \?[Dealer with DealerScene] with
//    \?[CardReceiver] with
//    \?[LastTrickWinner] with
//    \?[PlayerFaceForRound]
//  val playerMorphModel = parse[PlayerMorphType](true)
//
//
//  def main(args: Array[String]): Unit = {
//    val r = round()
//    val ts = System.currentTimeMillis()
//    for (i <- 0 until 100) {
//      //while (true) {
//      //val r = round()
//      val res = r.start()
//      //println(res)
//    }
//    val ts2 = System.currentTimeMillis()
//    println(s"Finished in ${ts2 - ts} msec")
//  }
//
//  def round() = {
//    val group = (0 to 3).map(i => new DefaultPerson(s"Player$i", Score(0, 0)))
//    new RoundContext(group, 0, None, 0)
//    //new RoundContextTest(group, 0, None, 0)
//  }
//}
//
//class RoundContext(group: IndexedSeq[Person], dealerNum: Int, maybeLastTrickWinnerNum: Option[Int], gameTrumpSuite: Int) {
//
//  import RoundContext._
//
//  private def newPlayerKernel(playerNum: Int): playerMorphModel.Kernel = {
//    implicit val personFrag = external[Person](group(playerNum))
//    implicit val playerSceneFrag = external[PlayerScene](new PlayerSceneImpl(playerNum))
//    implicit val dealerSceneFrag = external[DealerScene](new DealerSceneImpl)
//
//    singleton(playerMorphModel, rootStrategy(playerMorphModel))
//  }
//
//  val playerKernels = {
//    (0 until group.size).map(newPlayerKernel)
//  }
//
//  object RoundScene extends RoundScene {
//    lazy val dealer = asMorphOf[Player with PlayerFaceForRound with Dealer](playerKernels(dealerNum))
//
//    lazy val players = playerKernels.map(asMorphOf[Player with PlayerFaceForRound](_))
//
//    override val trumpSuite: Int = gameTrumpSuite
//  }
//
//  class PlayerSceneImpl(playerNum: Int) extends PlayerScene {
//    val roundScene = RoundScene
//    lazy val partner = asMorphOf[Player with PartnerFace](playerKernels(playerNum))
//    lazy val leftOpponent = asMorphOf[Player with OpponentFace](playerKernels((playerNum + 1) % 4))
//    lazy val rightOpponent = asMorphOf[Player with OpponentFace](playerKernels((playerNum + 2) % 4))
//    lazy val lastTrickWinner = maybeLastTrickWinnerNum match {
//      case None => None
//      case Some(lastTrickWinnerNum) => Some(asMorphOf[Player with LastTrickWinner](playerKernels(lastTrickWinnerNum)))
//    }
//    val trumpSuite: Int = 0 // todo
//  }
//
//  class DealerSceneImpl extends DealerScene {
//    lazy val cardHolders = playerKernels.map(asMorphOf[Player with CardReceiver](_))
//  }
//
//  val round = {
//    implicit val roundSceneFrag = external[RoundScene](RoundScene)
//    singleton[Round with RoundScene].!
//  }
//
//  def start(): Either[Int, Int] = {
//    round.start()
//  }
//
//}
