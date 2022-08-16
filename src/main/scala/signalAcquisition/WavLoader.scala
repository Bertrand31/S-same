package sesame

import javax.sound.sampled.AudioSystem
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}
import cats.implicits._
import cats.effect.IO

object WavLoader:

  def wavToByteArray(wavFile: File): IO[Array[Byte]] = IO {
    val audioInputStream = AudioSystem.getAudioInputStream(wavFile)
    val numBytes = Files.size(wavFile.toPath).toInt
    val audioBytes = new Array[Byte](numBytes)
    var numBytesRead = 0

    while numBytesRead =!= -1
    do numBytesRead = audioInputStream.read(audioBytes)

    audioBytes.reverse.dropWhile(_ === 0).reverse
  }
