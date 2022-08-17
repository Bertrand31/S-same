package sesame

import java.util.{ArrayList, Arrays}
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import scala.math.BigInt
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect.IO
import org.rocksdb.{ColumnFamilyDescriptor, ColumnFamilyHandle, DBOptions, RocksDB}

final case class StorageHandle(private val db: RocksDB) {

  def storeSong(footprint: ArraySeq[Long], songName: String): IO[Unit] =
    footprint.traverse_(hash =>
      IO { db.put(BigInt(hash).toByteArray, songName.getBytes(UTF_8)) }
    )

  def lookupHash(hash: Long): IO[Option[String]] =
    IO {
      Option(db.get(BigInt(hash).toByteArray)).map(new String(_, UTF_8))
    }
}

object Storage:

  private val storageFolder = new File("./rocksdb").getAbsolutePath

  private val rocksDBOptions =
    new DBOptions()
      .setCreateIfMissing(true)
      .setCreateMissingColumnFamilies(true)

  def setup: IO[StorageHandle] =
    val cfHandles = new ArrayList[ColumnFamilyHandle]()
    val columnFamilies = Arrays.asList(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY))
    IO { RocksDB.open(rocksDBOptions, storageFolder, columnFamilies, cfHandles) }
      .map(StorageHandle(_))


