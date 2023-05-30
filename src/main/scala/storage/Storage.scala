package sesame

import java.util.{ArrayList, Arrays}
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.MapHasAsScala
import cats.implicits._
import cats.effect.IO
import com.aerospike.client._
import com.aerospike.client.policy.{Policy, WritePolicy}
import com.aerospike.client.policy.GenerationPolicy
import com.aerospike.client.cdt._
import utils.MathUtils._

object StorageConstants {

  val MetadataNamespace = "metadata"
  val MetadataSet = "metadata"

  // Collocated with metadata because of Aerospike CE's limitation to 2 namespaces
  val IDNamespace = MetadataNamespace
  val IDSet = "ids"
  val IDDefaultRecord = "song-id"
  val SingleCounterBin = "id-bin"

  val FooprintNamespace = "footprint"
  val FootprintSet = null
  val FootprintBin = "" // single-bin namespace implies empty bin name
}

trait AerospikeHandler(private val db: AerospikeClient) {

  final def release: IO[Unit] =
    IO { db.close() }
}

final case class FootprintDB(private val db: AerospikeClient) extends AerospikeHandler(db) {

  private val writePolicy = new WritePolicy()
  writePolicy.sendKey = true
  writePolicy.generationPolicy = GenerationPolicy.NONE

  def storeSong(songId: Int, footprint: ArraySeq[Long]): IO[Unit] =
    footprint.zipWithIndex.traverse_({
      case (hash, index) =>
        val data = BigInt(shortToByteArray(index.toShort) ++ intToByteArray(songId)).toLong
        val key = Key(StorageConstants.FooprintNamespace, StorageConstants.FootprintSet, hash)
        val value = Bin(StorageConstants.FootprintBin, data)
        IO(db.put(writePolicy, key, value))
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
}

final case class MetadataDB(private val db: AerospikeClient) extends AerospikeHandler(db) {

  private val writePolicy = new WritePolicy()
  writePolicy.sendKey = true
  writePolicy.generationPolicy = GenerationPolicy.NONE

  def storeSong(metadata: Map[String, String]): IO[Int] = {
    val idKey = Key(StorageConstants.IDNamespace, StorageConstants.IDSet, StorageConstants.IDDefaultRecord)
    val incrementCounter = Bin(StorageConstants.SingleCounterBin, 1)
    IO {
      db.operate(
        writePolicy,
        idKey,
        Operation.add(incrementCounter),
        Operation.get(StorageConstants.SingleCounterBin),
      )
    }.map(_.getInt(StorageConstants.SingleCounterBin)).flatMap(songId =>
      val key = Key(StorageConstants.MetadataNamespace, StorageConstants.MetadataSet, songId)
      val bins = metadata.map({
        case (metaKey, metaVal) => Bin(metaKey, metaVal)
      }).toArray
      IO(db.put(writePolicy, key, bins*)).as(songId)
    )
  }

  private val readPolicy = new Policy()

  def getSong(id: Long): IO[Option[Map[String, String]]] = {
    val key = Key(StorageConstants.MetadataNamespace, StorageConstants.MetadataSet, id)
    IO(Option(db.get(readPolicy, key)))
      .map(_.map(_.bins.asScala.map(_.bimap(identity, _.toString)).toMap))
  }
}

object Storage:

  def setup: IO[(FootprintDB, MetadataDB)] =
    IO {
      val client = new AerospikeClient("0.0.0.0", 3000)
      (
        FootprintDB(client),
        MetadataDB(client),
      )
    }


