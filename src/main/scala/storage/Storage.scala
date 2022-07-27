package sesame

import java.nio.ByteBuffer
import java.util._
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import cats.implicits._
import cats.effect._
import org.rocksdb._

final case class StorageHandle(private val db: RocksDB) {

  def storeSong(footprint: Array[Long], songName: String): IO[Unit] =
    footprint
      .toList
      .foldMap(hash => IO { db.put(BigInt(hash).toByteArray, songName.getBytes(UTF_8)) }) >>
    IO.println(s"$songName was ingested")


  def lookupHash(hash: Long): IO[Option[String]] =
    IO {
      Option(db.get(BigInt(hash).toByteArray)).map(new String(_, UTF_8))
    }
}

object Storage:

  private val dbFolder: File = new File("./rocksdb")

  def setup: IO[StorageHandle] =
    val col1Name = "col1".getBytes(UTF_8)
    val col2Name = "col2".getBytes(UTF_8)
    val cfHandles = new ArrayList[ColumnFamilyHandle]()
    val opt =
      new DBOptions()
        .setCreateIfMissing(true)
        .setCreateMissingColumnFamilies(true)
    IO {
      RocksDB.open(
        opt,
        dbFolder.getAbsolutePath(),
        Arrays.asList(
          new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
          new ColumnFamilyDescriptor(col1Name),
          new ColumnFamilyDescriptor(col2Name),
        ),
        cfHandles,
      )
    }.map(StorageHandle(_))


