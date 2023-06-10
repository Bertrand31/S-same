package sesame

import org.scalatest._, flatspec._, matchers._

import sesame.utils.FileUtils

class FileUtilsSpec extends AnyFlatSpec with should.Matchers:

  import cats.effect.unsafe.implicits.global

  "getFilesIn" should "get all the files in given directory" in {

    val result = FileUtils.getFilesIn("src/test/resources/").unsafeRunSync()
    result.map(_.toString) shouldBe List("src/test/resources/01. Strangers By Nature.wav")
  }
