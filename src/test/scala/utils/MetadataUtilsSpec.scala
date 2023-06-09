package sesame

import org.scalatest._
import flatspec._
import matchers._

import java.io.File
import sesame.utils.MetadataUtils

class MetadataUtilsSpec extends AnyFlatSpec with should.Matchers:

  import cats.effect.unsafe.implicits.global

  "getWavMetadata" should "retrieve all the expected metadata" in {

    val audioFile = new File("src/test/resources/01. Strangers By Nature.wav")

    val expectedMetadata = Map(
      "ENCODER"   -> "Lavf59.16.100",
      "ARTIST"    -> "Adele",
      "COPYRIGHT" -> "PMEDIA",
      "COMMENT"   -> "PMEDIA",
      "ISRC"      -> "USSM12105969",
      "TITLE"     -> "Strangers By Nature",
      "ALBUM"     -> "30",
      "YEAR"      -> "2021",
    )

    val result = MetadataUtils.getWavMetadata(audioFile).unsafeRunSync()
    result.toMap should contain theSameElementsAs expectedMetadata
  }
