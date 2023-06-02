package sesame

import org.scalatest._
import flatspec._
import matchers._

import java.io.File
import scala.util.Random

class SoundFootprintGeneratorSpec extends AnyFlatSpec with should.Matchers:

  import cats.effect.unsafe.implicits.global

  "Ingestion, retrieval cycle" should "return 100% match" in {

    val audioFile = new File("src/test/resources/01. Strangers By Nature.wav")

    val resultsIO =
      for {
        dbs         <- Storage.setup
        (footprintDB, metadataDB) = dbs
        audioChunks <- WavLoader.wavToByteChunks(audioFile)
        footprint    = SoundFootprintGenerator.transform(audioChunks)
        _           <- footprintDB.storeSong(123456, footprint)
        results     <- Sésame.getMatchingSongs(footprint)(using footprintDB, metadataDB)
        _           <- footprintDB.release
        _           <- metadataDB.release
      } yield results

    val expected = List("==> foobar <==================\n- Hashes matching at 100.0%\n- Linearity matching at: 99.44%\n")
    resultsIO.unsafeRunSync() shouldBe expected
  }
