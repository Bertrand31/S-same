package sesame

import java.io.File
import java.nio.ByteBuffer
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import org.lwjgl.util.xxhash.XXHash._
import utils.{SoundPlayback, FileUtils}

object SoundLoader extends IOApp:

  private val InputSongsDirectory = new File("./data/processedFiles")

  private def debug(audioFile: File): IO[Unit] =
    for {
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      chunkList = audioChunks.toArray
      songName     = audioFile.getName().split('.').init.mkString("")
      // _           <- FileUtils.writeBytesToFile(new File(s"data/bytesData/$songName.csv"), chunkList.flatten)
      footprint    = SoundFootprintGenerator.toFourier(chunkList.iterator)
      _           <- FileUtils.writeComplexToFile(new File(s"data/footprintData/$songName.csv"), footprint)
    } yield ()

  private def processAndStoreSong(audioFile: File)(
    using footprintsDB: FootprintsDB,
    metadataDB: MetadataDB,
  ): IO[Unit] =
    for {
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      footprint    = SoundFootprintGenerator.transform(audioChunks)
      songName     = audioFile.getName().split('.').init.mkString("")
      songId       = XXH64(ByteBuffer.wrap(songName.getBytes), 123)
      _           <- footprintsDB.storeSong(songId, footprint)
      _           <- metadataDB.storeSong(songId, Map("name" -> songName))
      _           <- IO.println(s"$songName was ingested successfuly")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      databaseHandles            <- Storage.setup
      (given FootprintsDB, given MetadataDB) =  databaseHandles
      audioFiles                 <- FileUtils.getFilesIn(InputSongsDirectory)
      _                          <- IO.parTraverseN(5)(audioFiles)(processAndStoreSong)
      // _                       <- audioFiles.traverse_(debug)
    } yield ExitCode.Success
