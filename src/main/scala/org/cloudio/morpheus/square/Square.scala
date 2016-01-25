package org.cloudio.morpheus.square

import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 27/09/15.
 */

@fragment
trait Rectangle {
  var sides: (Float, Float) = _
}

@fragment
trait Square {
  this: Rectangle =>

  def side = {
    require(sides != null && sides._1 == sides._2, "Not a square")
    sides._1
  }
}


object App {

  def main(args: Array[String]) {

    val rectModel = parse[Rectangle with \?[Square]](false)
    val rectStr = promote[Square](rectModel)({
      case None => None
      case Some(proxy) if proxy.sides != null && proxy.sides._1 == proxy.sides._2 => Some(0)
      case _ => None
    })
    val rectKernel = singleton(rectModel, rectStr)

    def printRect(): Unit = {
      select[Square](rectKernel.~) match {
        case None => println(s"Rectangle ${rectKernel.~.sides}")
        case Some(rect) => println(s"Square ${rect.side}")
      }
    }

    rectKernel.~.sides = (10f, 10f)
    rectKernel.~.remorph
    printRect()

    rectKernel.~.sides = (20f, 10f)
    rectKernel.~.remorph
    printRect()
  }

}
