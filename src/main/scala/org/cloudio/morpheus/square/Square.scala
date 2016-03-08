package org.cloudio.morpheus.square

import org.morpheus._
import org.morpheus.Morpheus._

/**
  * Created by zslajchrt on 27/09/15.
  */

@fragment
trait Rectangle {
  var sides: (Float, Float) = (0, 0)
}

@fragment
trait Square {
  this: Rectangle =>

  def side = {
    require(sides != null)
    require(sides._1 == sides._2, "Not a square")
    sides._1
  }
}

@fragment
trait Ellipse {
  var axes: (Float, Float) = (0, 0)
}

@fragment
trait Circle {
  this: Ellipse =>

  def radius() = {
    require(axes != null)
    require(axes != null && axes._1 == axes._2, "Not a circle")
    axes._1
  }
}

@fragment
trait Segment {
  this: Rectangle | Ellipse =>

  def length: Float = {
    select[Ellipse](this) match {
      case Some(e) =>
        if (e.axes._1 != 0) e.axes._1 else e.axes._2
      case None =>
        select[Rectangle](this) match {
          case Some(r) =>
            if (r.sides._1 != 0) r.sides._1 else r.sides._2
          case None =>
            sys.error("Unexpected")
        }
    }
  }

  def length_=(l: Float): Unit = {
    select[Ellipse](this) match {
      case Some(e) =>
        if (e.axes._1 != 0) e.axes = (l, 0) else e.axes = (0, l)
      case None =>
        select[Rectangle](this) match {
          case Some(r) =>
            if (r.sides._1 != 0) r.sides = (l, 0) else r.sides = (0, l)
          case None =>
            sys.error("Unexpected")
        }
    }
  }

}

object App {

  def main1(args: Array[String]) {

    val rectModel = parse[Rectangle with \?[Square]](false)
    val rectStr = promote[Square](rectModel)({
      case None => None
      case Some(proxy) if proxy.sides != null && proxy.sides._1 == proxy.sides._2 => Some(0)
      case _ => None
    })
    val rectKernel = singleton(rectModel, rectStr)

    def printRect(): Unit = {
      select[Square](rectKernel.~) match {
        case Some(rect) => println(s"Square ${rect.side}")
        case None => println(s"Rectangle ${rectKernel.~.sides}")
      }
    }

    rectKernel.~.sides = (10f, 10f)
    rectKernel.~.remorph
    printRect()

    rectKernel.~.sides = (20f, 10f)
    rectKernel.~.remorph
    printRect()
  }

  def main(args: Array[String]): Unit = {
    val rectModel = parse[Rectangle with (Unit | Square | Segment)](true)
    val rectStr = maskFull[Unit | Square | Segment](rectModel)(rootStrategy(rectModel), {
      case None => Some(2)
      case Some(proxy) if proxy.sides != null && proxy.sides._1 == proxy.sides._2 => Some(1)
      case Some(proxy) if proxy.sides != null && (proxy.sides._1 == 0 || proxy.sides._2 == 0) => Some(2)
      case _ => Some(0)
    })
    val finalStrat = strict(rectStr)
    val rectKernel = singleton(rectModel, finalStrat)

    def printRect(): Unit = {
      select[Square](rectKernel.~) match {
        case Some(rect) => println(s"Square ${rect.side}")
        case None =>
          select[Segment](rectKernel.~) match {
            case Some(seg) => println(s"Segment ${seg.length}")
            case None =>
              println(s"Rectangle ${rectKernel.~.sides}")
          }
      }
    }

    val rectMorph = rectKernel.~
    rectMorph.sides = (10f, 10f)
    rectMorph.remorph
    println(rectMorph.myAlternative)
    printRect()

    rectMorph.sides = (20f, 10f)
    rectMorph.remorph
    println(rectMorph.myAlternative)
    printRect()

    rectMorph.sides = (0f, 10f)
    rectMorph.remorph
    println(rectMorph.myAlternative)
    printRect()

    var savedLength = select[Segment](rectMorph) match {
      case Some(seg) => seg.length
      case None => sys.error("Unexpected")
    }

    val model2Ref: &?[$[Ellipse] with (Unit | Segment | $[Circle])] = rectMorph
    val (model, defaultStrategy) = unveil(model2Ref)
    val ellipseStr = maskFull[Unit | Segment | Circle](model)(defaultStrategy, {
      case None => select[Segment](rectMorph) match {
        case None => None
        case Some(s) => Some(1)
      }
      case Some(proxy) if proxy.axes != null && (proxy.axes._1 == 0 || proxy.axes._2 == 0) => Some(1)
      case Some(proxy) if proxy.axes != null && (proxy.axes._1 == proxy.axes._2) => Some(2)
      case _ => Some(0)
    })
    val finalEllipseStr = strict(ellipseStr)
    val model2Kernel = *(model2Ref, finalEllipseStr, single[Ellipse], single[Circle])
    val morph2 = model2Kernel.~

    select[Segment](morph2) match {
      case Some(seg) => seg.length = savedLength
      case None => sys.error("Unexpected")
    }

    def printEllipse(): Unit = {
      select[Segment](model2Kernel.~) match {
        case Some(seg) => println(s"Segment ${seg.length}")
        case None =>
          select[Circle](model2Kernel.~) match {
            case Some(cir) => println(s"Circle ${cir.radius()}")
            case None =>
              println(s"Ellipse")
          }
      }
    }

    println(morph2.myAlternative)
    printEllipse()

    morph2.axes = (20f, 10f)
    morph2.remorph
    println(morph2.myAlternative)
    printEllipse()

    morph2.axes = (10f, 10f)
    morph2.remorph
    println(morph2.myAlternative)
    printEllipse()

    morph2.axes = (0f, 10f)
    morph2.remorph
    println(morph2.myAlternative)
    printEllipse()

    savedLength = select[Segment](morph2) match {
      case Some(seg) => seg.length
      case None => sys.error("Unexpected")
    }

    val model3Ref: &?[$[Rectangle] * (Unit | $[Square] | Segment)] = morph2
    val model3Kernel = *(model3Ref, finalStrat, single[Rectangle], single[Square])
    val morph3 = model3Kernel.~

    select[Segment](morph3) match {
      case Some(seg) => seg.length = savedLength
      case None => sys.error("Unexpected")
    }

    println(morph3.myAlternative)
    printRect()
    println(morph3.sides)

  }

}

