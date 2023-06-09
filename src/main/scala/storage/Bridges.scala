package sesame.storage

import scala.collection.immutable.ArraySeq
import cats.effect.IO
import sesame.types.{SongId, SongMetadata}

trait MetadataClient:

  def storeSongMetadata(metadata: SongMetadata): IO[SongId]

  def getSongMetadata(id: SongId): IO[Option[SongMetadata]]

trait FootprintClient:

  def storeSongFootprint(songId: SongId, footprint: ArraySeq[(Long, Int)]): IO[Unit]

  def lookupFootprintHash(hash: Long): IO[Option[(SongId, Short)]]

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

