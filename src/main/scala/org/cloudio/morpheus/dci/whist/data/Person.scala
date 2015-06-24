package org.cloudio.morpheus.dci.whist.data

/**
 * Created by zslajchrt on 23/06/15.
 */

case class Score(wins: Int, losses: Int)

trait Person {
  def name: String
  def score: Score
  def addWin(): Unit
  def addLoss(): Unit
}


class DefaultPerson(val name: String, val initialScore: Score) extends Person {

  private var score_ : Score = initialScore

  override def score: Score = score_

  override def addWin(): Unit = score_ = score_.copy(wins = score_.wins + 1)

  override def addLoss(): Unit = score_ = score_.copy(losses = score_.losses + 1)

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

object CardOrdering extends CardOrdering(0)
