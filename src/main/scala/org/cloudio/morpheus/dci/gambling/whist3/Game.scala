//package org.cloudio.morpheus.dci.gambling.whist3
//
//import org.cloudio.morpheus.dci.gambling.objects._
//import org.morpheus.Morpheus._
//import org.morpheus._
//
///**
// * Created by zslajchrt on 28/06/15.
// */
//trait Game {
//}
//
//@fragment
//trait WhistPlayer {
//  this: Gambler with Game =>
//
//  protected var holding: List[Card] = Nil
//  protected var tricks = List.empty[List[Card]]
//
//  def trickCount = tricks.length
//
//  protected def won() {
//    increaseWins(price)
//  }
//
//  protected def lost() {
//    increaseLosses(price)
//  }
//}
//
//@fragment
//trait Partner {
//  this: WhistPlayer =>
//
//  def swapCard(card: Card): Card = {
//    // todo
//    card
//  }
//
//}
//
//@fragment
//trait Opponent {
//  this: WhistPlayer =>
//}
//
//@fragment
//trait Dealer {
//  this: WhistPlayer =>
//}
//
//@fragment
//trait DealerScene {
//  def leftOpponent: Opponent
//  def rightOpponent: Opponent
//}
//
//@fragment
//trait PlayerScene {
//  def dealer: Dealer with Partner
//  def opponent2: Opponent
//  def price: BigDecimal
//}