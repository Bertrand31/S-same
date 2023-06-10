package sesame

import org.scalatest._, flatspec._, matchers._

import sesame.types.SongMetadata
import sesame.storage.MetadataBridge
import sesame.storage.aerospike.AeroClient

class MetadataStorageSpec extends AnyFlatSpec with should.Matchers with AerospikeDocker:

  import cats.effect.unsafe.implicits.global

  "Aerospike" should "correctly store and retrieve metadata from storage" in {

    val expectedMetadata = SongMetadata(Map("ARTIST" -> "Bertrand", "TITLE" -> "Hello"))

    Thread.sleep(2000)

    val test =
      AeroClient.setup.use(aeroClient =>
        given AeroClient = aeroClient
        MetadataBridge.storeSong(expectedMetadata).flatMap(songId =>
          MetadataBridge.getSong(songId).map((songId -> _))
        )
      )

    val (songId, metadata) = test.unsafeRunSync()
    songId.value shouldBe 1
    metadata shouldBe Some(expectedMetadata)
  }

