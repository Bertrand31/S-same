package sesame

import java.util.{ArrayList, Arrays}
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.MapHasAsScala
import cats.implicits._
import cats.effect.IO
import io.circe.syntax._
import io.circe.parser.decode
import com.aerospike.client._
import com.aerospike.client.policy.{Policy, WritePolicy}
import utils.MathUtils._

object StorageConstants {

  val MetadataNamespace = "metadata"
  val MetadataSet = "metadata"

  val FooprintNamespace = "footprint"
  val FootprintSet = "footprint"
  val FootprintBin = "slice-id"
}

final case class FootprintDB(private val db: AerospikeClient) {

  private val writePolicy = new WritePolicy()
  writePolicy.sendKey = true

  def storeSong(id: Int, footprint: ArraySeq[Long]): IO[Unit] =
    footprint.zipWithIndex.traverse_({
      case (hash, index) =>
        val data = BigInt(shortToByteArray(index.toShort) ++ intToByteArray(id)).toLong
        val key = Key(StorageConstants.FooprintNamespace, StorageConstants.FootprintSet, hash)
        val value = Bin(StorageConstants.FootprintBin, data)
        IO { db.put(writePolicy, key, value) }
    })

  private val readPolicy = new Policy()

  def lookupHash(hash: Long): IO[Option[(Short, Int)]] = {
    val key = Key(StorageConstants.FooprintNamespace, StorageConstants.FootprintSet, hash)
    IO(Option(db.get(readPolicy, key)))
      .map(_.map(record =>
        val bytes = BigInt(record.getLong(StorageConstants.FootprintBin)).toByteArray
        val (idxBytes, songIdBytes) = bytes.splitAt(2)
        (byteArrayToShort(idxBytes), byteArrayToInt(songIdBytes))
      ))
  }

  def release: IO[Unit] =
    IO { db.close() }
}

final case class MetadataDB(private val db: AerospikeClient) {

  private val writePolicy = new WritePolicy()
  writePolicy.sendKey = true

  def storeSong(id: Long, metadata: Map[String, String]): IO[Unit] = {
    val key = Key(StorageConstants.MetadataNamespace, StorageConstants.MetadataSet, id)
    val bins = metadata.map({
      case (metaKey, metaVal) => Bin(metaKey, metaVal)
    }).toArray
    IO(db.put(writePolicy, key, bins*))
  }

  private val readPolicy = new Policy()

  def getSong(id: Long): IO[Option[Map[String, String]]] = {
    val key = Key(StorageConstants.MetadataNamespace, StorageConstants.MetadataSet, id)
    IO(Option(db.get(readPolicy, key)))
      .map(_.map(_.bins.asScala.map(_.bimap(identity, _.toString)).toMap))
  }

  def release: IO[Unit] =
    IO { db.close() }
}

object Storage:

  def setup: IO[(FootprintDB, MetadataDB)] =
    IO {
      val client = new AerospikeClient("127.0.0.1", 3000)
      (
        FootprintDB(client),
        MetadataDB(client),
      )
    }


