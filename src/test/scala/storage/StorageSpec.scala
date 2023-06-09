package sesame

import org.scalatest._
import flatspec._
import matchers._

import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import sesame.types.SongId
import sesame.storage.FootprintBridge
import sesame.storage.aerospike.AeroClient

class StorageSpec extends AnyFlatSpec with should.Matchers with AerospikeDocker:

  import cats.effect.unsafe.implicits.global

  "Aerospike" should "correctly store and retrieve hashes from storage" in {

    val hashes = ArraySeq(123, 456, 789, Long.MaxValue, 0)

    Thread.sleep(2000)

    val test =
      AeroClient.setup.use(aeroClient =>
        given AeroClient = aeroClient
        for {
          _                <- FootprintBridge.storeSong(SongId(123456), hashes)
          idxHashes        =  hashes.zipWithIndex
          matches          <- idxHashes
                                .traverse({ case (hash, _) => FootprintBridge.lookupHash(hash) })
        } yield idxHashes.zip(matches).foreach({
          case ((_, idx), Some((_, matchIdx))) => idx shouldBe matchIdx
          case _ => throw new Error()
        })
    )

    test.unsafeRunSync()
  }

