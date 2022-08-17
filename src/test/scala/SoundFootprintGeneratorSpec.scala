package sesame

import org.scalatest._
import flatspec._
import matchers._
import scala.util.Random

class SoundFootprintGeneratorSpec extends AnyFlatSpec with should.Matchers:

  "The footprinting mechanism" should "return correct hashes" in {

    val song = List(
      Array(-53, 112, -21, 111, -95, -55, 86, 1, 3, 121, -32, -56, -118, -85, 16, -13).map(_.toByte)
    ).iterator

    println(SoundFootprintGenerator.transform(song).toList)
  }
