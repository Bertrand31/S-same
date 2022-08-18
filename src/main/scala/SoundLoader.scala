package sesame

import java.io.File
import cats.implicits._
import cats.effect._
import utils.{SoundPlayback, FileUtils}

object SoundLoader extends IOApp:

  private val InputSongsDirectory = new File("./data")

  private def processAndStoreSong(audioFile: File)(using db: StorageHandle): IO[Unit] =
    for {
      audioChunks <- WavLoader.wavToByteChunks(audioFile)
      // _           <- audioChunks.toList.traverse_(SoundPlayback.playWavByteArray)
      footprint    = SoundFootprintGenerator.transform(audioChunks)
      songName     = audioFile.getName().split('.').init.mkString("")
      _           <- db.storeSong(footprint, songName)
      _           <- IO.println(s"$songName was ingested successfuly")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      given StorageHandle <- Storage.setup
      audioFiles          <- FileUtils.getFilesIn(InputSongsDirectory)
      _                   <- IO.parTraverseN(5)(audioFiles)(processAndStoreSong)
    } yield ExitCode.Success
