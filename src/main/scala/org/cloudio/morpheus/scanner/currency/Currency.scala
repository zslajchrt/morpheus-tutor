package org.cloudio.morpheus.scanner.currency

import org.cloudio.morpheus.scanner.protodata.Thing.ThingMorphType
import org.cloudio.morpheus.scanner.protodata._
import org.json4s.JsonAST.{JNothing, JValue}
import org.json4s.native.JsonMethods
import org.morpheus._
import org.morpheus.Morpheus._
import org.morpheus.FragmentValidator._

import scala.util.Try

/**
 * Created by zslajchrt on 19/08/15.
 */

@dimension
trait Currency {
  def currency: Option[IdentifiedCurrency]
}

@fragment
trait Coin extends Currency {
  this: Metal with Cylinder with CurrencyIdentification =>

  override lazy val currency = identifyCoin(cylinderData.get.diameter, cylinderData.get.height)
}

@fragment
trait Banknote extends Currency {
  this: Paper with Rectangle with CurrencyIdentification =>

  override lazy val currency = identifyBanknote(rectangleData.get.width, rectangleData.get.height)

}

case class IdentifiedCurrency(description: String, currencyValue: BigDecimal, currencyCode: String)

@dimension
trait CurrencyIdentification {

  def identifyCoin(coinDiameter: Float, coinThickness: Float): Option[IdentifiedCurrency]

  def identifyBanknote(width: Float, height: Float): Option[IdentifiedCurrency]

}

@fragment
trait CurrencyIdentificationMock extends CurrencyIdentification {
  override def identifyCoin(coinDiameter: Float, coinThickness: Float): Option[IdentifiedCurrency] = None

  override def identifyBanknote(width: Float, height: Float): Option[IdentifiedCurrency] = Some(IdentifiedCurrency("Dollar 100", BigDecimal(100), "USD"))
}

@fragment
trait CurrencyIdentificationMock2 extends CurrencyIdentification {
  override def identifyCoin(coinDiameter: Float, coinThickness: Float): Option[IdentifiedCurrency] = Some(IdentifiedCurrency("25 Cents", BigDecimal(0.25), "USD"))

  override def identifyBanknote(width: Float, height: Float): Option[IdentifiedCurrency] = None
}

@fragment
trait CurrencyIdentificationMock3 extends CurrencyIdentification {
  override def identifyCoin(coinDiameter: Float, coinThickness: Float): Option[IdentifiedCurrency] = None

  override def identifyBanknote(width: Float, height: Float): Option[IdentifiedCurrency] = None
}

@fragment
trait CurrencyIdentificationMock4 extends CurrencyIdentification {
  override def identifyCoin(coinDiameter: Float, coinThickness: Float): Option[IdentifiedCurrency] = Some(IdentifiedCurrency("25 Cents", BigDecimal(0.25), "USD"))

  override def identifyBanknote(width: Float, height: Float): Option[IdentifiedCurrency] = Some(IdentifiedCurrency("Dollar 100", BigDecimal(100), "USD"))
}

@fragment
trait CurrencyValidator {
  this: Currency =>

  def validateCurrency = validationResult[Currency](currency, "Unidentified")
}


object Currency {
  type CurrencyMorphModel = (Coin or Banknote) with CurrencyIdentificationMock4
  val currencyModel = parse[CurrencyMorphModel](false)

  def newCurrency(thingSrc: JValue): &[Coin or Banknote] = {
    val morphStrategy = EnableValidFragmentsOnlyStrategy(rootStrategy(currencyModel))

    val thing = *(Thing.newThing(thingSrc))
    val currencyThingRef: &[$[CurrencyMorphModel]] = thing
    val currency = *(currencyThingRef, morphStrategy, single[Coin], single[Banknote], single[CurrencyIdentificationMock4])

    val validatorRef: &[$[CurrencyValidator]] = currency
    val validator = *(validatorRef, single[CurrencyValidator])
    val valResults: Iterable[ValidationResult[Currency]] = for (validatorMorph <- validator if validatorMorph.isSuccess)
      yield validatorMorph.get.validateCurrency

    morphStrategy.updateValidFragments(valResults)

    currency
  }
}

object CurrencyApp {

  def main(args: Array[String]): Unit = {
    val thingSrc1 = """
      {
        "thing": {
          "thingId": 123,
          "scannerId": 19393,
          "scanTime": "2012-04-23T00:00:00Z",
          "luggageId": 19092003
        },
        "cylinder": {
          "diameter": 13.45,
          "height": 0.45
        }
        "metal": {}
      }
    """
    val thingSrc2 = """
      {
        "thing": {
          "thingId": 123,
          "scannerId": 19393,
          "scanTime": "2012-04-23T00:00:00Z",
          "luggageId": 19092003
        },
        "rectangle": {
          "width": 13.45,
          "height": 0.45
        }
        "paper": {}
      }
    """
    val thingJson = JsonMethods.parseOpt(thingSrc1).getOrElse(JNothing)
    processThing(thingJson)
  }

  def processThing(thingSrc: JValue): Unit = {
    val currencyRef: &[Coin or Banknote] = Currency.newCurrency(thingSrc)
    val currency = *(currencyRef)
    val m = currency.make
    currency.maybeMake match {
      case None =>
      case Some(morph) => processCurrency(morph)
    }
  }

  def processCurrency(currencyMorph: Currency): Unit = {
    println(currencyMorph.currency.get)
  }

}