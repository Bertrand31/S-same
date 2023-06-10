package sesame

import org.scalatest._, flatspec._, matchers._

import java.io.File
import scala.collection.immutable.ArraySeq
import sesame.types.SongId
import sesame.footprint.SoundFootprintGenerator
import sesame.storage.FootprintBridge
import sesame.storage.aerospike.AeroClient

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
          corresponding    <- SoundMatcher.findCorrespondingHashes(footprint)
          groupedCorr       = SoundMatcher.groupOffsetsBySong(corresponding)
          rankedMatches     = SoundMatcher.rankMatches(footprint.size)(groupedCorr)
        } yield rankedMatches
      )

    val expected = ArraySeq((SongId(123456), 100.0f, 3.9775624f)) // TODO: figure out the 3.9
    val results = resultsIO.unsafeRunSync()
    results should contain theSameElementsInOrderAs expected
  }
}
