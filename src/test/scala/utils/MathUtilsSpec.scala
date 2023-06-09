package sesame

import org.scalatest._
import flatspec._
import matchers._

import sesame.utils.MathUtils

class MathUtilsSpec extends AnyFlatSpec with should.Matchers:

  "toPaddedBytesArray" should "translate an integer correctly" in {

    MathUtils.toPaddedBytesArray(123456) shouldBe Array[Byte](0, 1, -30, 64)
  }

  "roundtrips" should "return the original integer" in {

    val numbers = List(
      123456,
      0,
      Int.MaxValue
    )
    numbers.foreach(n =>
      MathUtils.byteArrayToInt(MathUtils.toPaddedBytesArray(n)) shouldBe n
    )
  }

  "toPaddedBytesArray" should "translate a short correctly" in {

    MathUtils.toPaddedBytesArray(12345.toShort) shouldBe Array[Byte](48, 57)
  }

  "roundtrips" should "return the original short" in {

    val numbers = List[Short](
      987,
      0,
      Short.MaxValue
    )
    numbers.foreach(n =>
      MathUtils.byteArrayToShort(MathUtils.toPaddedBytesArray(n)) shouldBe n
    )
  }
