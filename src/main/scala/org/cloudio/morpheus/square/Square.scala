package org.cloudio.morpheus.square

import java.io.PrintWriter

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 27/09/15.
  */

trait ShapeVisitor {

  def visitRectangle(r: Rectangle): Unit

  def visitSquare(s: Rectangle with Square): Unit
}

trait Shape {
  def accept(sv: ShapeVisitor): Unit

  def area: Double

  def move(dx: Double, dy: Double): Unit
}

class ShapePainter(gc: Graphics) extends ShapeVisitor {

  override def visitRectangle(r: Rectangle): Unit = {
    gc.drawRect(r.x, r.y, r.width, r.height)
    val radius = Math.sqrt((r.width / 2) * (r.width / 2) + (r.height / 2) * (r.height / 2))
    gc.drawCircle(r.x + r.width / 2, r.y + r.height / 2, radius)
  }

  override def visitSquare(s: Rectangle with Square): Unit = {
    visitRectangle(s)
    val radius = s.side / 2
    gc.drawCircle(s.x + s.side / 2, s.y + s.side / 2, radius)
  }
}

@fragment
trait Rectangle extends Shape {
  var x: Double = 0
  var y: Double = 0
  var width: Double = 0
  var height: Double = 0

  def accept(sv: ShapeVisitor): Unit = {
    sv.visitRectangle(this)
  }

  override def area: Double = width * height

  override def move(dx: Double, dy: Double): Unit = {
    x += dx
    y += dy
  }
}

@fragment
trait Square {
  this: Rectangle =>

  def side = width

  def side_=(s: Double): Unit = {
    this.width = s
    this.height = s
  }
}

@fragment
@wrapper
trait SquareW extends Rectangle {
  this: Square =>
  override def accept(sv: ShapeVisitor): Unit = {
    sv.visitSquare(this)
  }
}

@fragment
trait Point {
}

@fragment
trait Ellipse {
  var axisX: Double = 0
  var axisY: Double = 0
}

@fragment
trait Circle {
  this: Ellipse =>

  def radius = this.axisX

  def radius_=(r: Double): Unit = {
    this.axisX = r
    this.axisY = r
  }
}

@fragment
trait Segment {
  this: Rectangle | Ellipse =>

  def length: Double = {
    select[Ellipse](this) match {
      case Some(e) =>
        if (e.axisX != 0) e.axisX else e.axisY
      case None =>
        select[Rectangle](this) match {
          case Some(r) =>
            if (r.width != 0) r.width else r.height
          case None =>
            sys.error("Unexpected")
        }
    }
  }

  def length_=(l: Double): Unit = {
    select[Ellipse](this) match {
      case Some(e) =>
        if (e.axisX != 0) e.axisX = l else e.axisY = l
      case None =>
        select[Rectangle](this) match {
          case Some(r) =>
            if (r.width != 0) {
              r.width = l
              r.height = 0
            } else {
              r.width = 0
              r.height = l
            }
          case None =>
            sys.error("Unexpected")
        }
    }
  }

}

object App {

  def main(args: Array[String]) {
    val out = new PrintWriter("drawing2.svg")
    val g = new SVGGraphics(out)
    g.start()
    val painter = new ShapePainter(g)

    val rectModel = parse[Rectangle with \?[Square with SquareW]](false)

    val rectStg = promote[Square](rectModel)({
      case None => Some(0)
      case Some(rect) if rect.width == rect.height => Some(0)
      case _ => None
    })

    val rectRkg = singleton(rectModel, rectStg)
    val rect = rectRkg.~

    rect.width = 100
    rect.height = 50
    rect.remorph

    // draw
    rect.accept(painter)

    // make the square
    rect.y = 150
    rect.height = 100
    rect.remorph
    rect.accept(painter)

    val sq: Square with Rectangle = select[Square with Rectangle](rect).get
    sq.y = 350
    sq.side = 80
    rect.accept(painter)

    sq.x = 200
    sq.height = 50
    rect.remorph
    rect.accept(painter)

    g.end()
    out.close()
  }

  def main1(args: Array[String]) {

    val rectModel = parse[Rectangle with \?[Square with SquareW]](false)

    val rectStg = promote[Square](rectModel)({
      case None => Some(0)
      case Some(rect) if rect.width == rect.height => Some(0)
      case _ => None
    })

    val rectRkg = singleton(rectModel, rectStg)
    val rect = rectRkg.~

    // do not present in the paper
    def printRect(r: rectModel.MutableLUB): Unit = {
      select[Square](r) match {
        case Some(sq) => print(s"Square(${sq.side}):")
        case None =>
      }
      println(s"Rectangle(${r.width},${r.height})")
    }

    printRect(rect)

    rect.width = 10f
    rect.remorph
    printRect(rect)

    rect.height = 10f
    rect.remorph
    printRect(rect)

    select[Square](rect) match {
      case Some(sq) => sq.side = 20f
      case None => sys.error("Unexpected")
    }
    printRect(rect)

    rect.height = 5f
    rect.remorph
    printRect(rect)
  }

  def main2(args: Array[String]): Unit = {
    val rectModel = parse[Rectangle with (Unit | Square | Segment)](true)

    def rectStg(initShape: Int) = {
      val rectStr = maskFull[Unit | Square | Segment](rectModel)(rootStrategy(rectModel), {
        case None => Some(initShape)
        case Some(rect) if rect.width == rect.height => Some(1)
        case Some(rect) if rect.width == 0 || rect.height == 0 => Some(2)
        case _ => Some(0)
      })
      strict(rectStr)
    }

    val rectRkg = singleton(rectModel, rectStg(1))
    val rect = rectRkg.~

    // do not present in the paper
    def printRect(r: rectModel.MutableLUB): Unit = {
      select[Square](r) match {
        case Some(rect) => print(s"Square(${rect.side}):")
        case None =>
          select[Segment](r) match {
            case Some(seg) => print(s"Segment(${seg.length}):")
            case None =>
          }
      }
      println(s"Rectangle(${r.width},${r.height})")
    }

    printRect(rect)

    rect.height = 10f
    rect.remorph
    printRect(rect)

    rect.width = 20f
    rect.remorph
    printRect(rect)

    rect.width = 0f
    rect.remorph
    printRect(rect)

    var savedSide = select[Segment](rect) match {
      case Some(seg) => seg.length
      case None => sys.error("Unexpected")
    }

    val rectRef: &?[$[Ellipse] with (Unit | Segment | $[Circle])] = rect

    val (ellipseModel, ellipseDefStg) = unveil(rectRef)

    def ellipseStg(initShape: Int) = {
      val stg = maskFull[Unit | Segment | Circle](ellipseModel)(ellipseDefStg, {
        case None => Some(initShape)
        case Some(ellipse) if ellipse.axisX == 0 || ellipse.axisX == 0 => Some(1)
        case Some(ellipse) if ellipse.axisX == ellipse.axisY => Some(2)
        case _ => Some(0)
      })
      strict(stg)
    }

    val ellipseRkg = *(rectRef, ellipseStg(1), single[Ellipse], single[Circle])

    val ellipse = ellipseRkg.~

    select[Segment](ellipse) match {
      case Some(seg) => seg.length = savedSide
      case None => sys.error("Unexpected")
    }

    // do not present in the paper
    def printEllipse(el: ellipseModel.MutableLUB): Unit = {
      select[Segment](el) match {
        case Some(seg) => print(s"Segment(${seg.length}):")
        case None =>
          select[Circle](el) match {
            case Some(cir) => print(s"Circle(${cir.radius}):")
            case None =>
          }
      }
      println(s"Ellipse(${el.axisX},${el.axisY})")
    }

    printEllipse(ellipse)

    ellipse.axisX = 20f
    ellipse.remorph
    printEllipse(ellipse)

    ellipse.axisY = 20f
    ellipse.remorph
    printEllipse(ellipse)

    ellipse.axisX = 0f
    ellipse.remorph
    printEllipse(ellipse)

    savedSide = select[Segment](ellipse) match {
      case Some(seg) => seg.length
      case None => sys.error("Unexpected")
    }

    val ellipseRef: &?[$[Rectangle] * (Unit | $[Square] | Segment)] = ellipse

    val rect2Kernel = *(ellipseRef, rectStg(2), single[Rectangle], single[Square])
    val rect2 = rect2Kernel.~

    select[Segment](rect2) match {
      case Some(seg) => seg.length = savedSide
      case None => sys.error("Unexpected")
    }

    printRect(rect2)

    rect2.width = 10f
    rect2.height = 10f
    rect2.remorph

    printRect(rect2)

    rect2.height = 30f
    rect2.remorph

    printRect(rect2)
  }

  def main3(args: Array[String]): Unit = {
    type Model = (Rectangle with (Unit | Square | Segment | Point)) or (Ellipse with (Unit | Circle | Segment | Point))
    val shapeModel = parse[Model](true)

    var ellipseContextActive = false

    val shapeStg = {
      val stg1 = unmaskAll(rootStrategy(shapeModel))
      val stg2 = maskFull_+[Model](shapeModel)(stg1, {
        case None => if (ellipseContextActive) Some(7) else Some(3)
        case Some(s) => select[Rectangle](s) match {
          case Some(rect) if rect.width == 0 && rect.height == 0 =>
            if (ellipseContextActive) Some(7) else Some(3) // point
          case Some(rect) if rect.width == rect.height => Some(1) // square
          case Some(rect) if rect.width == 0 || rect.height == 0 => Some(2) // segment
          case Some(rect) => Some(0) // pure rectangle
          case None => select[Ellipse](s) match {
            case Some(ellipse) if ellipse.axisX == 0 && ellipse.axisY == 0 =>
              if (ellipseContextActive) Some(7) else Some(3) // point
            case Some(ellipse) if ellipse.axisX == ellipse.axisY => Some(5) // circle
            case Some(ellipse) if ellipse.axisX == 0 || ellipse.axisY == 0 => Some(6) // segment
            case Some(ellipse) => Some(4) // pure ellipse
            case None => sys.error("Unexpected")
          }
        }
      })
      strict(stg2)
    }

    val shapeRkg = singleton(shapeModel, shapeStg)
    val shape = shapeRkg.~

    // do not present in the paper
    def printShape(sh: shapeModel.MutableLUB): Unit = {
      select[Rectangle](sh) match {
        case Some(r) =>
          select[Square](sh) match {
            case Some(sq) => print(s"Square(${sq.side}):")
            case None =>
              select[Segment](sh) match {
                case Some(seg) => print(s"Segment(${seg.length}):")
                case None =>
                  select[Point](sh) match {
                    case Some(seg) => print(s"Point():")
                    case None =>
                  }
              }
          }
          println(s"Rectangle(${r.width},${r.height})")
        case None =>
          select[Ellipse](sh) match {
            case Some(el) =>
              select[Circle](sh) match {
                case Some(c) => print(s"Circle(${c.radius}):")
                case None =>
                  select[Segment](sh) match {
                    case Some(seg) => print(s"Segment(${seg.length}):")
                    case None =>
                      select[Point](sh) match {
                        case Some(seg) => print(s"Point():")
                        case None =>
                      }
                  }
              }
              println(s"Ellipse(${el.axisX},${el.axisY})")
            case None => sys.error("Unexpected")
          }
      }
    }

    printShape(shape)

    select[Rectangle](shape) match {
      case None => sys.error("Unexpected")
      case Some(rect) =>
        rect.height = 10f
        shape.remorph
        printShape(shape)

        rect.width = 20f
        shape.remorph
        printShape(shape)

        rect.height = 20f
        shape.remorph
        printShape(shape)

        rect.width = 0f
        shape.remorph
        printShape(shape)

        ellipseContextActive = true
        rect.height = 0f
        shape.remorph
        printShape(shape)
    }

    select[Ellipse](shape) match {
      case None => sys.error("Unexpected")
      case Some(ellipse) =>
        ellipse.axisX = 10f
        shape.remorph
        printShape(shape)

        ellipse.axisY = 20f
        shape.remorph
        printShape(shape)

        ellipse.axisX = 20f
        shape.remorph
        printShape(shape)

        ellipse.axisX = 0f
        shape.remorph
        printShape(shape)

        ellipseContextActive = false
        ellipse.axisY = 0f
        shape.remorph
        printShape(shape)
    }

    //
    //    var savedSide = select[Segment](rect) match {
    //      case Some(seg) => seg.length
    //      case None => sys.error("Unexpected")
    //    }
    //
    //    val rectRef: &?[$[Ellipse] with (Unit | Segment | $[Circle])] = rect
    //
    //    val (ellipseModel, ellipseDefStg) = unveil(rectRef)
    //
    //    def ellipseStg(initShape: Int) = {
    //      val stg = maskFull[Unit | Segment | Circle](ellipseModel)(ellipseDefStg, {
    //        case None => Some(initShape)
    //        case Some(ellipse) if ellipse.axisX == 0 || ellipse.axisX == 0 => Some(1)
    //        case Some(ellipse) if ellipse.axisX == ellipse.axisY => Some(2)
    //        case _ => Some(0)
    //      })
    //      strict(stg)
    //    }
    //
    //    val ellipseRkg = *(rectRef, ellipseStg(1), single[Ellipse], single[Circle])
    //
    //    val ellipse = ellipseRkg.~
    //
    //    select[Segment](ellipse) match {
    //      case Some(seg) => seg.length = savedSide
    //      case None => sys.error("Unexpected")
    //    }
    //
    //    // do not present in the paper
    //    def printEllipse(el: ellipseModel.MutableLUB): Unit = {
    //      select[Segment](el) match {
    //        case Some(seg) => print(s"Segment(${seg.length}):")
    //        case None =>
    //          select[Circle](el) match {
    //            case Some(cir) => print(s"Circle(${cir.radius}):")
    //            case None =>
    //          }
    //      }
    //      println(s"Ellipse(${el.axisX},${el.axisY})")
    //    }
    //
    //    printEllipse(ellipse)
    //
    //    ellipse.axisX = 20f
    //    ellipse.remorph
    //    printEllipse(ellipse)
    //
    //    ellipse.axisY = 20f
    //    ellipse.remorph
    //    printEllipse(ellipse)
    //
    //    ellipse.axisX = 0f
    //    ellipse.remorph
    //    printEllipse(ellipse)
    //
    //    savedSide = select[Segment](ellipse) match {
    //      case Some(seg) => seg.length
    //      case None => sys.error("Unexpected")
    //    }
    //
    //    val ellipseRef: &?[$[Rectangle] * (Unit | $[Square] | Segment)] = ellipse
    //
    //    val rect2Kernel = *(ellipseRef, rectStg(2), single[Rectangle], single[Square])
    //    val rect2 = rect2Kernel.~
    //
    //    select[Segment](rect2) match {
    //      case Some(seg) => seg.length = savedSide
    //      case None => sys.error("Unexpected")
    //    }
    //
    //    printShape(rect2)
    //
    //    rect2.width = 10f
    //    rect2.height = 10f
    //    rect2.remorph
    //
    //    printShape(rect2)
    //
    //    rect2.height = 30f
    //    rect2.remorph
    //
    //    printShape(rect2)
  }

}

