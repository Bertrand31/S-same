package sesame

import java.io.File
import cats.implicits._
import cats.effect._

object SoundLoader extends IOApp:

  private val InputSongsDirectory = new File("./data")

  private def processAndStoreSong(audioFile: File)(implicit db: StorageHandle): IO[Unit] =
    WavLoader.wavToByteArray(audioFile).flatMap(byteArray =>
      val footprint = SoundFootprintGenerator.transform(byteArray)
      val songName = audioFile.getName().split('.').init.mkString("")
      db.storeSong(footprint, songName)
    )

  def run(args: List[String]): IO[ExitCode] =
    for {
      given StorageHandle <- Storage.setup
      audioFiles          <- FileUtils.getFilesIn(InputSongsDirectory)
      _                   <- audioFiles.traverse(processAndStoreSong)
    } yield ExitCode.Success
