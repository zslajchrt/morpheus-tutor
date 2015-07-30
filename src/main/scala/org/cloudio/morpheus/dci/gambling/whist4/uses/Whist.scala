package org.cloudio.morpheus.dci.gambling.whist4.uses

import org.morpheus._
import org.morpheus.Morpheus._
import org.cloudio.morpheus.dci.gambling.whist4.data._
import org.cloudio.morpheus.dci.gambling.objects._

import scala.util.Random

/**
*
* Created by zslajchrt on 23/06/15.
*/

// objective roles

@fragment
trait Player {
  protected var holding: List[Card] = Nil
  protected var tricks = List.empty[List[Card]]

  def trickCount = tricks.length
}

@fragment
trait PlayerFaceForRound {

  this: Person with Player with PlayerScene with PlayerLogic =>

  def reset(): Unit = {
    holding = Nil
    tricks = Nil
  }

  def addTrick(trick: List[Card]): Unit = {
    tricks ::= trick
  }

  def playCard(trick: List[Card]): Card = {
    val maybeLastTrick: Option[List[Card]] = for (ltw <- lastTrickWinner) yield ltw.showLastTrick
    var selectedCard = selectCard(maybeLastTrick)

    // remove the selected card from the deck
    removeCard(selectedCard)

    // if the selected card is not the highest in the trick then
    // ask the partner to swap the selected card for the partner's best corresponding one.
    //val ord: CardOrdering = roundScene.cardOrd
    val ord: CardOrdering = CardOrdering
    if (!trick.forall(card => ord.compare(card, selectedCard) > 0)) {
      selectedCard = partner.swapCards(selectedCard) match {
        case None => selectedCard
        case Some(swapped) => swapped
      }
    }

    selectedCard
  }

  private def removeCard(card: Card): Unit = {
    val selectedCardIndex = holding.indexOf(card)
    val splitCards = holding.splitAt(selectedCardIndex)
    holding = splitCards._1 ::: splitCards._2.tail
  }

  def won(): Unit = {
    addWin()
  }

  def lost(): Unit = {
    addLoss()
  }
}

@dimension
trait PlayerLogic {
  def selectCard(lastTrick: Option[List[Card]]): Card
}

@fragment
trait RobotPlayerLogic extends PlayerLogic {
  this: Player =>

  override def selectCard(lastTrick: Option[List[Card]]): Card = {
    // todo
    holding.head
  }
}

@fragment
trait LivePlayerLogic extends PlayerLogic {
  this: Player =>

  override def selectCard(lastTrick: Option[List[Card]]): Card = {
    // todo
    holding.head
  }
}

@fragment
trait Dealer {
  this: DealerScene =>

  def deal(cards: List[Card]): Unit = {
    for (cardNum <- 0 until cards.size;
         cardHolder = cardHolders(cardNum % 4);
         card = cards(cardNum)) {
      cardHolder.receiveCard(card)
    }
  }

}

@fragment
trait CardReceiver {
  this: Player =>

  def receiveCard(card: Card): Unit = {
    holding ::= card
  }
}

@fragment
trait LastTrickWinner {
  this: Player =>

  def showLastTrick: List[Card] = {
    tricks.head
  }
}

// subjective roles

@fragment
trait PartnerFace {
  this: Player with PlayerScene =>

  def swapCards(card: Card): Option[Card] = {
    //val ord: CardOrdering = roundScene.cardOrd
    val ord: CardOrdering = CardOrdering
    holding.find(myCard => {
      ord.compare(card, myCard) > 0
    }) match {
      case None => None
      case c => c
    }
  }
}

@fragment
trait OpponentFace {
}

@fragment
trait Round {

  this: RoundScene =>

  def start(): Either[Int, Int] = {
    for (p <- players) {
      p.reset()
    }

    val cards1 = shuffle(Card.deck)
    //cards1 = dealer.nextPlayer.shuffle(cards1)
    // opt.
    //cards1 = dealer.shuffle(cards1)
    // opt. let the dealer's partner shuffle the second deck of cards
    //cards2 = cardHolders((cardHolders.indexOf(dealer) + 2) % 4).shuffle(cards2)
    dealer.deal(cards1)

    for (trickNum <- 0 to 12) {

      var trick = List.empty[Card]
      for (playerNum <- trickNum to (trickNum + 3);
           playerNumMod = playerNum % 4) {
        trick ::= validatePlayedCard(players(playerNumMod).playCard(trick))
      }

      val winnerNum = (trickNum + trick.indexOf(trick.max(new CardOrdering(trumpSuite)))) % 4
      val winner = players(winnerNum)
      winner.addTrick(trick)
    }

    val leftScore = players(0).trickCount + players(2).trickCount
    val rightScore = players(1).trickCount + players(3).trickCount

    if (leftScore > rightScore) {
      players(0).won()
      players(2).won()
      players(1).lost()
      players(3).lost()
      Left(leftScore - 6)
    } else {
      players(0).lost()
      players(2).lost()
      players(1).won()
      players(3).won()
      Right(rightScore - 6)
    }
    //Left(0)
  }

  private def validatePlayedCard(card: Card) = {
    // todo:
    card
  }

  private def shuffle(deck: List[Card]): List[Card] = {
    val rnd = new Random

    // a possible bug in Scala/Java7: this does not work under jdk1.7.0_80
    //deck.sortBy(c => rnd.nextInt())
    deck.map(c => (c, rnd.nextInt())).sortWith(_._2 < _._2).map(_._1)
  }


}

trait PlayerScene {
  def roundScene: RoundScene

  def partner: Player with PartnerFace

  def leftOpponent: Player with OpponentFace

  def rightOpponent: Player with OpponentFace

  def lastTrickWinner: Option[Player with LastTrickWinner]
}

trait DealerScene {
  def cardHolders: IndexedSeq[Player with CardReceiver]
}

trait RoundScene {

  def dealer: Player with PlayerFaceForRound with Dealer

  def players: IndexedSeq[Player with PlayerFaceForRound]

  val trumpSuite: Int

  lazy val cardOrd = new CardOrdering(trumpSuite)

}

//object RoundContext {
//
//  type PlayerMorphType = Person with Player with PlayerScene with
//    (LivePlayerLogic or RobotPlayerLogic) with
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
