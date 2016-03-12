package org.cloudio.morpheus.square.classic

import java.io.{FileWriter, PrintWriter}

import org.cloudio.morpheus.square.{Graphics, SVGGraphics}

/**
  * Created by zslajchrt on 12/03/16.
  */
object Square {

}


trait Shape {
  def accept(sv: ShapeVisitor): Unit

  def area: Double
}

class Rectangle extends Shape {

  var x: Double = 0
  var y: Double = 0
  var width: Double = 0
  var height: Double = 0

  def accept(sv: ShapeVisitor): Unit = {
    sv.visitRectangle(this)
  }

  override def area: Double = width * height
}

class Square extends Rectangle {

  def side = width

  def side_=(s: Float) = {
    this.width = s
    this.height = s
  }

  override def accept(sv: ShapeVisitor): Unit = {
    sv.visitSquare(this)
  }
}

trait ShapeVisitor {

  def visitRectangle(r: Rectangle): Unit

  def visitSquare(s: Square): Unit
}

class ShapePainter(gc: Graphics) extends ShapeVisitor {

  override def visitRectangle(r: Rectangle): Unit = {
    gc.drawRect(r.x, r.y, r.width, r.height)
    val radius = Math.sqrt((r.width / 2) * (r.width / 2) + (r.height / 2) * (r.height / 2))
    gc.drawCircle(r.x + r.width / 2, r.y + r.height / 2, radius)
  }

  override def visitSquare(s: Square): Unit = {
    visitRectangle(s)
    val radius = s.side / 2
    gc.drawCircle(s.x + s.side / 2, s.y + s.side / 2, radius)
  }
}

object App {

  def main(args: Array[String]) {
    val out = new PrintWriter("drawing.svg")
    val g = new SVGGraphics(out)
    g.start()
    val painter = new ShapePainter(g)

    val rect = new Rectangle
    rect.width = 100
    rect.height = 50

    // draw
    rect.accept(painter)

    val sq = new Square
    sq.y = 150
    sq.side = 100

    // draw
    sq.accept(painter)

    sq.x = 200
    sq.height = 50

    // draw
    sq.accept(painter)

    g.end()
    out.close()
  }

}