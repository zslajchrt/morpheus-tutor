package org.cloudio.morpheus.square

/**
  * Created by zslajchrt on 12/03/16.
  */
trait Graphics {

  def drawRect(x: Double, y: Double, w: Double, h: Double): Unit

  def drawCircle(x: Double, y: Double, r: Double): Unit

}
