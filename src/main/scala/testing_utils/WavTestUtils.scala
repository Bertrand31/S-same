package sesame.utils

import java.io._
import javax.sound.sampled._
import cats.effect.IO
import sesame.audio.MicRecorder
import sesame.audio.AudioCommons.InputFormat

object WavUtils:

  def writeToFile(inputData: Array[Byte]): IO[Unit] =
    IO {
      val byteArrayInputStream = new ByteArrayInputStream(inputData)
      val audioInputStream = AudioInputStream(byteArrayInputStream, InputFormat, inputData.size)
      val file = new File("lastRecording.wav")
      AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file)
    }

  def listenAndWriteToDisk(): IO[Unit] =
    MicRecorder.recordChunks.map(_.flatten.toArray).flatMap(writeToFile)

