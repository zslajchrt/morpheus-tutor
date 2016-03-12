package org.cloudio.morpheus.square

/**
  * Created by zslajchrt on 12/03/16.
  */
class DummyGraphics extends Graphics {

  override def drawRect(x: Double, y: Double, w: Double, h: Double): Unit = {
    println(s"Rectangle($x,$y,$w,$h)")
  }

  override def drawCircle(x: Double, y: Double, r: Double): Unit = {
    println(s"Circle($x,$y,$r)")
  }
}
