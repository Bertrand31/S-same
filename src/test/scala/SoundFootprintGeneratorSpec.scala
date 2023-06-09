package sesame

import org.scalatest._
import flatspec._
import matchers._

import java.io.File
import scala.util.Random

class SoundFootprintGeneratorSpec extends AnyFlatSpec with should.Matchers with AerospikeDocker {
  self: Suite =>

  import cats.effect.unsafe.implicits.global

  "Ingestion, retrieval cycle" should "return 100% match" in {

    val audioFile = new File("src/test/resources/01. Strangers By Nature.wav")

    Thread.sleep(2000)

    val resultsIO =
      AeroClient.setup.use(aeroClient =>
        given AeroClient = aeroClient
        for {
          audioChunks      <- WavLoader.wavToByteChunks(audioFile)
          footprint         = SoundFootprintGenerator.transform(audioChunks)
          _                <- FootprintBridge.storeSong(SongId(123456), footprint)
          results          <- Sésame.getMatchingSongs(footprint)
        } yield results
      )

    val expected = List("==> foobar <==================\n- Hashes matching at 100.0%\n- Linearity matching at: 99.44%\n")
    resultsIO.unsafeRunSync() shouldBe expected
  }
}
