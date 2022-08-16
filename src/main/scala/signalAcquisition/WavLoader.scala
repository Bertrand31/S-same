package sesame

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}
import javax.sound.sampled.{AudioFormat, AudioSystem}
import cats.implicits._
import cats.effect.IO

object WavLoader:

  def wavToByteArray(wavFile: File): IO[Array[Byte]] =
    IO { AudioSystem.getAudioFileFormat(wavFile) }.flatMap({
      case fileFormat if fileFormat.getFormat matches Commons.audioFormat =>
        IO.blocking {
          val audioInputStream = AudioSystem.getAudioInputStream(wavFile)
          val audioBytes = new Array[Byte](fileFormat.getByteLength())
          var numBytesRead = 0

          while numBytesRead =!= -1
          do numBytesRead = audioInputStream.read(audioBytes)

          audioBytes.dropWhile(_ === 0).reverse.dropWhile(_ === 0).reverse
        }
      case fileFormat =>
        IO.raiseError(AudioFormatException(s"$fileFormat did not match ${Commons.audioFormat}"))
    })
