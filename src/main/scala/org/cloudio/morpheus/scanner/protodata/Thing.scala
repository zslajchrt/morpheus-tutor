package org.cloudio.morpheus.scanner.protodata

import java.util.Date

import org.json4s.JValue
import org.json4s.JsonAST.{JValue, JString}
import org.morpheus._
import org.morpheus.Morpheus._
import org.morpheus.FragmentValidator._

/**
 * Created by zslajchrt on 19/08/15.
 */
object Thing {
  type ThingMorphType = Thing with (Rectangle or Cylinder) with (Paper or Plastic or Metal)
  val thingModel = parse[ThingMorphType](true)


  def newThing(srcJson: JValue): &![ThingMorphType] = {
    val morphStrategy = EnableValidFragmentsOnlyStrategy(rootStrategy(thingModel))
    val thing = singleton(thingModel, morphStrategy)
    val valRes: Iterable[ValidationResult[_]] = Loader.load(srcJson, thing)
    morphStrategy.updateValidFragments(valRes)
    thing
  }
}

case class ThingData(thingId: Int, scannerId: Int, scanTime: Date, luggageId: Int)

@fragment
trait Thing {
  var thingData = Option.empty[ThingData]
}

@dimension
trait Shape {
}

case class RectangleData(width: Float, height: Float)

@fragment
trait Rectangle extends Shape {
  var rectangleData = Option.empty[RectangleData]
}

case class CylinderData(diameter: Float, height: Float)

@fragment
trait Cylinder extends Shape {
  var cylinderData = Option.empty[CylinderData]
}


@dimension
trait Material {
}

@fragment
trait Paper extends Material {
}

@fragment
trait Plastic extends Material {
}

@fragment
trait Metal extends Material {
}


/// Loaders

import org.json4s.JsonAST.{JArray, JNothing}
import org.json4s.native.JsonMethods
import org.json4s.{DefaultFormats, JValue}
import Thing._

@dimension
trait Loader[F] {
  def load(srcJson: JValue): ValidationResult[F]
}

object Loader {
  implicit val formats = DefaultFormats

  type LoaderMorphType = (ThingLoader or CylinderLoader or RectangleLoader or PaperLoader or MetalLoader)

  def load(srcJson: JValue, thingRef: &![ThingMorphType]): Iterable[ValidationResult[_]] = {
    val loadersThingRef: &[$[LoaderMorphType]] = *(thingRef)
    val loaders = *(loadersThingRef, single[ThingLoader], single[CylinderLoader], single[RectangleLoader], single[PaperLoader], single[MetalLoader])
    for (loader <- loaders) yield loader.get.load(srcJson)
  }

}
import Loader._

@fragment
trait ThingLoader extends Loader[Thing] {
  this: Thing =>

  def load(srcJson: JValue) = {
    thingData = srcJson.\("thing").extractOpt[ThingData]
    validationResult[Thing](thingData, "Invalid content")
  }
}

@fragment
trait CylinderLoader extends Loader[Cylinder] {
  this: Cylinder =>

  def load(srcJson: JValue) = {
    cylinderData = srcJson.\("cylinder").extractOpt[CylinderData]
    validationResult[Cylinder](cylinderData, "Invalid content")
  }
}

@fragment
trait RectangleLoader extends Loader[Rectangle] {
  this: Rectangle =>

  def load(srcJson: JValue) = {
    rectangleData = srcJson.\("rectangle").extractOpt[RectangleData]
    validationResult[Rectangle](rectangleData, "Invalid content")
  }
}

@fragment
trait PaperLoader extends Loader[Paper] {
  this: Paper =>

  def load(srcJson: JValue) = {
    srcJson \ "paper" match {
      case JNothing => failure[Paper]("Invalid content")
      case _ => success[Paper]
    }
  }
}

@fragment
trait MetalLoader extends Loader[Metal] {
  this: Metal =>

  def load(srcJson: JValue) = {
    val value: JValue = srcJson \ "metal"
    value match {
      case JNothing => failure[Metal]("Invalid content")
      case _ => success[Metal]
    }
  }
}
