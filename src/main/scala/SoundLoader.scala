package sesame

import java.io.File
import scala.util.hashing.MurmurHash3
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import utils.{MetadataUtils, SoundPlayback, FileUtils}

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
      using footprintsDB: FootprintDB,
      metadataDB: MetadataDB,
  ): IO[Unit] =
    for {
      metadata    <- MetadataUtils.getWavMetadata(audioFile)
      songId      <- metadataDB.storeSong(metadata)
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      footprint    = SoundFootprintGenerator.transform(audioChunks)
      _           <- footprintsDB.storeSong(songId, footprint)
      songName     = metadata.getOrElse(MetadataUtils.songTitleKey, "Unknown title")
      _           <- IO.println(s"$songName was ingested successfuly")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      databaseHandles                       <- Storage.setup
      (given FootprintDB, given MetadataDB) =  databaseHandles
      audioFiles                            <- FileUtils.getFilesIn(InputSongsDirectory)
      _                                     <- IO.parTraverseN(5)(audioFiles)(processAndStoreSong)
      // _                                  <- audioFiles.traverse_(debug)
    } yield ExitCode.Success
