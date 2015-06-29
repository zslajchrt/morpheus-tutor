package org.cloudio.morpheus.dci.gambling.whist4.data

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

