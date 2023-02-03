package sesame

import java.util.{ArrayList, Arrays}
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect.IO
import io.circe.syntax._
import io.circe.parser.decode
import org.rocksdb.{ColumnFamilyDescriptor, ColumnFamilyHandle, DBOptions, RocksDB}
import utils.MathUtils._

final case class FootprintsDB(private val db: RocksDB) {

  def storeSong(id: Long, footprint: ArraySeq[Long]): IO[Unit] =
    footprint.zipWithIndex.traverse_({
      case (hash, index) =>
        val key = BigInt(hash).toByteArray
        val value = charToByteArray(index.toChar) ++ longToByteArray(id)
        IO { db.put(key, value) }
    })

  def lookupHash(hash: Long): IO[Option[(Char, Long)]] =
    IO {
      Option(db.get(BigInt(hash).toByteArray))
    }.map(_.map(value =>
      val (idxBytes, songIdBytes) = value.splitAt(16)
      (byteArrayToChar(idxBytes), byteArrayToLong(songIdBytes))
    ))

  def release: IO[Unit] =
    IO { db.close() }
}

final case class MetadataDB(private val db: RocksDB) {

  def storeSong(id: Long, metadata: Map[String, String]): IO[Unit] = {
    val key = BigInt(id).toByteArray
    val value = metadata.asJson.noSpaces.getBytes
    IO { db.put(key, value) }
  }

  def getSong(id: Long): IO[Option[Map[String, String]]] =
    IO {
      Option(db.get(BigInt(id).toByteArray))
    }
      .map(_.map(new String(_, UTF_8)))
      .map(_.flatMap(decode[Map[String, String]](_).toOption)) // TODO: Add parsing error handling

  def release: IO[Unit] =
    IO { db.close() }
}

object Storage:

  private val footprintStorage = new File("./footprint").getAbsolutePath
  private val metadataStorage = new File("./metadata").getAbsolutePath

  private val rocksDBOptions =
    new DBOptions()
      .setCreateIfMissing(true)
      .setCreateMissingColumnFamilies(true)

  def setup: IO[(FootprintsDB, MetadataDB)] =
    val cfHandles = new ArrayList[ColumnFamilyHandle]()
    val footprintColumnFamilies = Arrays.asList(
      new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
      new ColumnFamilyDescriptor("footprint".getBytes),
    )
    val metadataColumnFamilies = Arrays.asList(
      new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
      new ColumnFamilyDescriptor("metadata".getBytes),
    )
    IO {
      (
        FootprintsDB(
          RocksDB.open(rocksDBOptions, footprintStorage, footprintColumnFamilies, cfHandles)
        ),
        MetadataDB(
          RocksDB.open(rocksDBOptions, metadataStorage, metadataColumnFamilies, cfHandles)
        ),
      )
    }


