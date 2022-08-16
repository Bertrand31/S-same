package sesame

import java.io.File
import cats.implicits._
import cats.effect._

object SoundLoader extends IOApp:

  private val InputSongsDirectory = new File("./data")

  private def processAndStoreSong(audioFile: File)(using db: StorageHandle): IO[Unit] =
    for {
      byteArray <- WavLoader.wavToByteArray(audioFile)
      footprint  = SoundFootprintGenerator.transform(byteArray)
      songName   = audioFile.getName().split('.').init.mkString("")
      _         <- db.storeSong(footprint, songName)
      _         <- IO.println(s"$songName was ingested successfuly")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      given StorageHandle <- Storage.setup
      audioFiles          <- FileUtils.getFilesIn(InputSongsDirectory)
      _                   <- audioFiles.traverse_(processAndStoreSong)
    } yield ExitCode.Success
