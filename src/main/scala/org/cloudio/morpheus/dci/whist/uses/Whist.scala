package org.cloudio.morpheus.dci.whist.uses

import org.morpheus._
import org.morpheus.Morpheus._
import org.cloudio.morpheus.dci.whist.data._

import scala.util.Random

/**
 * Created by zslajchrt on 23/06/15.
 */

// objective roles

@dimension @wrapper
trait Player extends Person {
  this: PlayerScene with CardSelector =>

  protected var cards: List[Card] = Nil
  private var tricks = List.empty[List[Card]]

  def shuffle(deck: List[Card]): List[Card] = {
    val rnd = new Random
    val (left, right) = deck.span(_ => rnd.nextBoolean())
    left ::: right
  }

  def playCard(trick: List[Card]): Card = {
    val selectedCard = selectCard(cards)
    val selectedCardIndex = cards.indexOf(selectedCard)
    val splitCards = cards.splitAt(selectedCardIndex)
    cards = splitCards._1 ::: splitCards._2.tail
    selectedCard
  }

  def receiveTrick(trick: List[Card]): Unit = {
    tricks ::= trick
  }

  def trickCount = tricks.size

  def nextPlayer = leftOpponent

}

@dimension
trait CardSelector {

  def selectCard(cards: List[Card]): Card

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
    cards ::= card
  }
}


// subjective roles

@fragment
trait Partner {
}

@fragment
trait Opponent {
}

trait PlayerScene {
  val partner: Player with Partner
  val leftOpponent: Player with Opponent
  val rightOpponent: Player with Opponent
  val trumpSuite: Int
}

trait DealerScene {
  val cardHolders: List[Player with CardReceiver]
}

trait Game {

  this: GameScene =>

  def start(): Either[Int, Int] = {
    var cards1 = Card.deck
    //var cards2 = Card.deck
    cards1 = dealer.nextPlayer.shuffle(cards1)
    // opt.
    cards1 = dealer.shuffle(cards1)
    // opt. let the dealer's partner shuffle the second deck of cards
    //cards2 = cardHolders((cardHolders.indexOf(dealer) + 2) % 4).shuffle(cards2)
    dealer.deal(cards1)

    for (trickNum <- 0 to 12) {

      var trick = List.empty[Card]
      for (playerNum <- trickNum to (trickNum + 3);
           playerNumMod = playerNum % 4) {
        trick ::= cardHolders(playerNumMod).playCard(trick)
      }

      val winnerNum = (trickNum + trick.indexOf(trick.max(new CardOrdering(trumpSuite)))) % 4
      val winner = cardHolders(winnerNum)
      winner.receiveTrick(trick)
    }

    val leftScore = cardHolders(0).trickCount + cardHolders(2).trickCount
    val rightScore = cardHolders(1).trickCount + cardHolders(3).trickCount

    if (leftScore > rightScore) {
      Left(leftScore - 6)
    } else {
      Right(rightScore - 6)
    }
  }

}

trait GameScene {
  val dealer: Player with Dealer
  val cardHolders: List[Player with CardReceiver]
  val trumpSuite: Int
}


object Card {
  val deck = {
    (for (suite <- 0 to 3; rank <- 0 to 12) yield Card(suite, rank)).toList
  }
}

case class Card(suite: Int, rank: Int)

class CardOrdering(trumpSuite: Int) extends Ordering[Card] {
  override def compare(x: Card, y: Card): Int = {
    if (x.suite == y.suite) {
      x.rank - y.rank
    } else if (x.suite == trumpSuite) {
      1
    } else if (y.suite == trumpSuite) {
      -1
    } else {
      0
    }
  }
}


object PlayerScene {

  def main(args: Array[String]) {
    val partnerModel = parse[MutablePerson with Player with Partner with PlayerScene with CardSelector](true)
  }


  //  type GenPlayer = Player with PlayerScene with \?[Dealer with DealerScene]
//  val partnerModel = parse[GenPlayer with Partner](true)
//  val opponentModel = parse[GenPlayer with Opponent](true)
//  val cardReceiverModel = parse[Player with PlayerScene with CardHolder](true)
}
