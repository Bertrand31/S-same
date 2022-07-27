package sesame

import javax.sound.sampled.{AudioFormat, AudioFileFormat, AudioSystem}
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}
import cats.implicits._
import cats.effect._
import javax.sound.sampled.AudioInputStream

object SoundLoader extends IOApp:

  private def wavToByteArray(filePath: String): IO[Array[Byte]] = IO {
    val fileIn = new File(filePath)
    val audioInputStream = AudioSystem.getAudioInputStream(fileIn)
    // val bytesPerFrame = audioInputStream.getFormat.getFrameSize
    val numBytes = Files.size(Paths.get(filePath)).toInt
    val audioBytes = new Array[Byte](numBytes)
    var numBytesRead = 0
    var numFramesRead = 0

    while (numBytesRead != -1) {
      numBytesRead = audioInputStream.read(audioBytes)
    }
    audioBytes.reverse.dropWhile(_ == 0).reverse
  }

  def run(args: List[String]): IO[ExitCode] =
    Storage.setup.flatMap(db =>
      List("./data/01. Strangers By Nature.wav", "./data/02. Easy On Me.wav")
        .traverse(filePath =>
          wavToByteArray(filePath).flatMap(byteArray =>
            val footprint = SoundFootprintGenerator.transform(byteArray)
            val songName = filePath.split('/').last.split('.').init.mkString("")
            db.storeSong(footprint, songName)
          )
        )
        .as(ExitCode.Success)
    )
