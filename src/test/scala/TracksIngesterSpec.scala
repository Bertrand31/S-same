package sesame

import org.scalatest._
import flatspec._
import matchers._

import java.io.File
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import com.whisk.docker.*
import com.whisk.docker.testkit.*
import com.spotify.docker.client.messages.PortBinding
import com.spotify.docker.client.messages.HostConfig.Bind
import com.whisk.docker.testkit.scalatest.DockerTestKitForAll

class TracksIngesterSpec extends AnyFlatSpec with should.Matchers with DockerTestKitForAll:

  override val managedContainers =
    ContainerSpec("aerospike:ce-6.3.0.4_1")
      .withName(s"${getClass.getSimpleName}-aerospike")
      .withPortBindings(
        3000 -> PortBinding.of("127.0.0.1", 3000),
        3001 -> PortBinding.of("127.0.0.1", 3001),
        3002 -> PortBinding.of("127.0.0.1", 3002),
      )
      .withReadyChecker(DockerReadyChecker.LogLineContains("soon there will be cake!"))
      .withVolumeBindings(Bind.from("/home/bertrand/Code/sésame/aerospike_conf").to("/opt/aerospike/etc/").build)
      .toContainer
      .toManagedContainer

  import cats.effect.unsafe.implicits.global

  "TracksIngester" should "ingest songs correctly" in {

    val hashes = ArraySeq(123, 456, 789, Long.MaxValue, 0)
    val songName = "Test Song"

    val test = for {
      databaseHandles           <- Storage.setup
      (footprintDB, metadataDB) = databaseHandles
      _           <- TracksIngester.processAndStoreSong(new File("./data/processedFiles/01. Strangers By Nature.wav"))(using footprintDB, metadataDB)
      _           <- footprintDB.release
      _           <- metadataDB.release
    } yield ()

    test.unsafeRunSync()
  }

