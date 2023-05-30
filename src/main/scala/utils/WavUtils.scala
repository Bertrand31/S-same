package sesame.utils

import java.io._
import javax.sound.sampled._
import cats.effect.IO

object WavUtils:

  import sesame.Commons.InputFormat

  def writeToFile(inputData: Array[Byte]): IO[Unit] =
    IO {
      val byteArrayInputStream = new ByteArrayInputStream(inputData)
      val audioInputStream = AudioInputStream(byteArrayInputStream, InputFormat, inputData.size)
      val file = new File("lastRecording.wav")
      AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file)
    }
