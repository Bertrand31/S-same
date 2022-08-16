package sesame

import org.scalatest._
import flatspec._
import matchers._
import cats.effect._

class StorageSpec extends AnyFlatSpec with should.Matchers:

  import cats.effect.unsafe.implicits.global

  val handle = Storage.setup.unsafeRunSync()

  "RocksDB" should "correctly store an retrieve hashes from storage" in {

    val song = Array(123, 456, 789, Long.MaxValue, 0)
    val songName = "Test Song"

    handle.storeSong(song, songName).unsafeRunSync()

    song.foreach(
      handle.lookupHash(_).unsafeRunSync() shouldBe Some(songName)
    )
  }
