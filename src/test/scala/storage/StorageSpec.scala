package sesame

import org.scalatest._
import flatspec._
import matchers._

import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._

class StorageSpec extends AnyFlatSpec with should.Matchers with AerospikeDocker:

  import cats.effect.unsafe.implicits.global

  "Aerospike" should "correctly store and retrieve hashes from storage" in {

    val hashes = ArraySeq(123, 456, 789, Long.MaxValue, 0)
    val songName = "Test Song"

    Thread.sleep(2000)

    val test = for {
      dbs         <- Storage.setup
      (footprintDB, metadataDB) = dbs
      _           <- footprintDB.storeSong(123456, hashes)
      idxHashes   =  hashes.zipWithIndex
      matches     <- idxHashes.traverse({ case (hash, _) => footprintDB.lookupHash(hash) })
      _           <- footprintDB.release
      _           <- metadataDB.release
    } yield idxHashes.zip(matches).foreach({
      case ((_, idx), Some((matchIdx, _))) => idx shouldBe matchIdx
      case _ => throw new Error()
    })

    test.unsafeRunSync()
  }
