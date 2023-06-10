package sesame

import org.scalatest._, flatspec._, matchers._, matchers.should.Matchers._

import java.io.File
import scala.collection.immutable.ArraySeq
import sesame.types.SongId
import sesame.footprint.SoundFootprintGenerator
import sesame.storage.FootprintBridge
import sesame.storage.aerospike.AeroClient

class SoundFootprintGeneratorSpec extends AnyFlatSpec with AerospikeDocker {
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
          gatheredSample    = footprint.drop(footprint.size / 3).take(footprint.size / 10)
          corresponding    <- SoundMatcher.findCorrespondingHashes(gatheredSample)
          groupedCorr       = SoundMatcher.groupOffsetsBySong(corresponding)
          rankedMatches     = SoundMatcher.rankMatches(gatheredSample.size)(groupedCorr)
        } yield (corresponding, groupedCorr, rankedMatches)
      )

    val (corresponding, groupedCorr, rankedMatches) = resultsIO.unsafeRunSync()

    groupedCorr.map(_._1) should contain theSameElementsAs List(SongId(123456))

    // Linearity not matching at 100% because of hash collisions
    rankedMatches match
      case ArraySeq((SongId(123456), 100f, linearity)) =>
        linearity should be > (99f)
      case result => throw new Exception(s"Incorrect match: $result")

  }
}
