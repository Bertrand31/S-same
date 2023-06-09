package sesame.storage.aerospike

import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.MapHasAsScala
import cats.implicits._
import cats.effect._
import com.aerospike.client._
import com.aerospike.client.policy._
import com.aerospike.client.policy.{Policy, WritePolicy}
import com.aerospike.client.policy.GenerationPolicy
import sesame.utils.MathUtils._
import sesame.types.{SongId, SongMetadata}
import sesame.storage.{FootprintClient, MetadataClient}

class AeroClient(private val client: AerospikeClient) extends MetadataClient with FootprintClient:

  private val metadataWritePolicy = new WritePolicy()
  metadataWritePolicy.sendKey = false // Doesn't store keys, only their digests
  metadataWritePolicy.generationPolicy = GenerationPolicy.NONE
  metadataWritePolicy.readModeSC = ReadModeSC.LINEARIZE
  metadataWritePolicy.commitLevel = CommitLevel.COMMIT_MASTER

  private val metadataReadPolicy = new Policy()

  private val IdKey = Key(
    AerospikeConstants.IDNamespace,
    AerospikeConstants.IDSet,
    AerospikeConstants.IDDefaultRecord,
  )

  def storeSongMetadata(metadata: SongMetadata): IO[SongId] =
    val incrementCounter = Bin(AerospikeConstants.SingleCounterBin, 1)
    IO {
      client.operate(
        metadataWritePolicy,
        IdKey,
        Operation.add(incrementCounter),
        Operation.get(AerospikeConstants.SingleCounterBin),
      )
    }.map(_.getInt(AerospikeConstants.SingleCounterBin)).map(SongId(_)).flatMap(songId =>
      val key = Key(AerospikeConstants.MetadataNamespace, AerospikeConstants.MetadataSet, songId.value)
      val bins = metadata.toMap.map({
        case (metaKey, metaVal) => Bin(metaKey, metaVal)
      }).toArray
      IO(client.put(metadataWritePolicy, key, bins*)).as(songId)
    )

  def getSongMetadata(id: SongId): IO[Option[SongMetadata]] =
    val key = Key(AerospikeConstants.MetadataNamespace, AerospikeConstants.MetadataSet, id.value)
    IO(Option(client.get(metadataReadPolicy, key)))
      .map(_.map(_.bins.asScala.map(_.bimap(identity, _.toString)).toMap))
      .map(_.map(SongMetadata(_)))

  private val footprintWritePolicy = new WritePolicy()
  footprintWritePolicy.sendKey = false // Doesn't store keys, only their digests
  footprintWritePolicy.generationPolicy = GenerationPolicy.NONE
  footprintWritePolicy.recordExistsAction = RecordExistsAction.REPLACE
  footprintWritePolicy.commitLevel = CommitLevel.COMMIT_MASTER

  private val footprintReadPolicy = new Policy()

  def storeSongFootprint(songId: SongId, footprint: ArraySeq[(Long, Int)]): IO[Unit] =
    footprint.traverse_({
      case (hash, index) =>
        val data = shortToByteArray(index.toShort) ++ intToByteArray(songId.value)
        val key = Key(AerospikeConstants.FooprintNamespace, AerospikeConstants.FootprintSet, hash)
        val value = Bin(AerospikeConstants.FootprintBin, data)
        IO(client.put(footprintWritePolicy, key, value))
    })

  def lookupFootprintHash(hash: Long): IO[Option[(SongId, Short)]] =
    val key = Key(AerospikeConstants.FooprintNamespace, AerospikeConstants.FootprintSet, hash)
    IO(Option(client.get(footprintReadPolicy, key)))
      .map(_.map(record =>
        val bytes = record.getValue(AerospikeConstants.FootprintBin).asInstanceOf[Array[Byte]]
        val (idxBytes, songIdBytes) = bytes.splitAt(2)
        (SongId(byteArrayToInt(songIdBytes)), byteArrayToShort(idxBytes))
      ))

  def release: IO[Unit] = IO { client.close() }

object AeroClient:

  def setup: Resource[IO, AeroClient] =
    Resource.make(
      IO(new AerospikeClient("0.0.0.0", 3000))
        .map(new AeroClient(_))
    )(_.release)
