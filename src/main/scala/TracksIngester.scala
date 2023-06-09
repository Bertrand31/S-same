package sesame

import java.io.File
import cats.implicits._
import cats.effect._
import utils.{MetadataUtils, FileUtils}

object TracksIngester extends IOApp:

  private val InputSongsDirectory = "./data/processedFiles"

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
