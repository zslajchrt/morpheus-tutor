package org.cloudio.morpheus.dci.gambling.objects

/**
 * Created by zslajchrt on 28/06/15.
 */
case class Card(suite: Int, rank: Int)

object Card {
  val deck = {
    (for (suite <- 0 to 3; rank <- 0 to 12) yield Card(suite, rank)).toList
  }
}

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
