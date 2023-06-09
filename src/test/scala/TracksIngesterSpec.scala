package sesame

import org.scalatest._
import flatspec._
import matchers._

import java.io.File
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._

class TracksIngesterSpec extends AnyFlatSpec with should.Matchers with AerospikeDocker:

  import cats.effect.unsafe.implicits.global

  "TracksIngester" should "ingest songs correctly" in {

    val hashes = ArraySeq(123, 456, 789, Long.MaxValue, 0)
    val songName = "Test Song"

    Thread.sleep(2000)

    val test =
      AeroClient.setup.use(aeroClient =>
        given AeroClient = aeroClient
        TracksIngester.processAndStoreSong(
          new File("./data/processedFiles/01. Strangers By Nature.wav")
        )
      )

    test.unsafeRunSync()
  }

