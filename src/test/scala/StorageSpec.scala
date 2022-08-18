package sesame

import org.scalatest._
import flatspec._
import matchers._

import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._

class StorageSpec extends AnyFlatSpec with should.Matchers:

  import cats.effect.unsafe.implicits.global

  "RocksDB" should "correctly store an retrieve hashes from storage" in {

    val hashes = ArraySeq(123, 456, 789, Long.MaxValue, 0)
    val songName = "Test Song"

    val test = for {
      dbHandle  <- Storage.setup
      _         <- dbHandle.storeSong(hashes, songName)
      idxHashes  = hashes.zipWithIndex
      matches   <- idxHashes.traverse({ case (hash, _) => dbHandle.lookupHash(hash) })
      _         <- dbHandle.release
    } yield idxHashes.zip(matches).foreach({
      case ((_, idx), Some(s"$songName", matchIdx)) => idx shouldBe matchIdx
      case _ => throw new Error()
    })

    test.unsafeRunSync()
  }

