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

object FootprintBridge:

  def storeSong(songId: SongId, footprint: ArraySeq[Long])(
      using footprintClient: FootprintClient
  ): IO[Unit] =
    footprintClient.storeSongFootprint(songId, footprint.zipWithIndex)

  def lookupHash(hash: Long)(using footprintClient: FootprintClient): IO[Option[(SongId, Short)]] =
    footprintClient.lookupFootprintHash(hash)

object MetadataBridge:

  def storeSong(metadata: SongMetadata)(using metadataClient: MetadataClient): IO[SongId] =
    metadataClient.storeSongMetadata(metadata)

  def getSong(id: SongId)(using metadataClient: MetadataClient): IO[Option[SongMetadata]] =
    metadataClient.getSongMetadata(id)

