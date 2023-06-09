package sesame

import java.io.File
import scala.util.hashing.MurmurHash3
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import utils.{MetadataUtils, SoundPlayback, FileUtils}

object TracksIngester extends IOApp:

  private val InputSongsDirectory = new File("./data/processedFiles")

  private def debug(audioFile: File): IO[Unit] =
    for {
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      chunkList    = audioChunks.toArray
      songName     = audioFile.getName().split('.').init.mkString("")
      // _           <- FileUtils.writeBytesToFile(new File(s"data/bytesData/$songName.csv"), chunkList.flatten)
      footprint    = SoundFootprintGenerator.toFourier(chunkList.iterator)
      _           <- FileUtils.writeComplexToFile(new File(s"data/footprintData/$songName.csv"), footprint)
    } yield ()

  def processAndStoreSong(audioFile: File)(
      using FootprintClient, MetadataClient,
  ): IO[Unit] =
    for {
      metadata    <- MetadataUtils.getWavMetadata(audioFile)
      songId      <- MetadataBridge.storeSong(metadata)
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      footprint    = SoundFootprintGenerator.transform(audioChunks)
      _           <- FootprintBridge.storeSong(songId, footprint)
      _           <- IO.println(s"${metadata.getTitle} was ingested successfuly")
    } yield ()

  private val ParallelismLevel = 5

  def run(args: List[String]): IO[ExitCode] =
    AeroClient.setup.use(aeroClient =>
      given AeroClient = aeroClient
      FileUtils.getFilesIn(InputSongsDirectory)
        .flatMap(IO.parTraverseN(ParallelismLevel)(_)(processAndStoreSong))
        .as(ExitCode.Success)
    )
        // _               <- audioFiles.traverse_(debug)
