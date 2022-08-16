package sesame

import java.util._
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import cats.implicits._
import cats.effect._
import org.rocksdb.{ColumnFamilyDescriptor, ColumnFamilyHandle, DBOptions, RocksDB}

final case class StorageHandle(private val db: RocksDB) {

  def storeSong(footprint: Array[Long], songName: String): IO[Unit] =
    footprint
      .toList
      .traverse_(hash => IO { db.put(BigInt(hash).toByteArray, songName.getBytes(UTF_8)) })

  def lookupHash(hash: Long): IO[Option[String]] =
    IO {
      Option(db.get(BigInt(hash).toByteArray)).map(new String(_, UTF_8))
    }
}

object Storage:

  private val dbFolder: File = new File("./rocksdb")

  def setup: IO[StorageHandle] =
    val cfHandles = new ArrayList[ColumnFamilyHandle]()
    val opt =
      new DBOptions()
        .setCreateIfMissing(true)
        .setCreateMissingColumnFamilies(true)
    IO {
      RocksDB.open(
        opt,
        dbFolder.getAbsolutePath(),
        Arrays.asList(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY)),
        cfHandles,
      )
    }.map(StorageHandle(_))


