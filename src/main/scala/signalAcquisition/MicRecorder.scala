package sesame

import java.io.{ByteArrayOutputStream, File}
import javax.sound.sampled.{AudioFormat, AudioFileFormat, AudioSystem}
import cats.effect._

object MicRecorder:

  private val audioFormat =
    val sampleRate = 44100F
    val sampleSizeInBits = 16
    val channels = 1
    val signed = true
    val bigEndian = false
    AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)

  def record: IO[Array[Byte]] =
    IO {
      val microphone = AudioSystem.getTargetDataLine(audioFormat)
      val out = new ByteArrayOutputStream()
      microphone.open(audioFormat)
      var bytesRead = 0
      val data = new Array[Byte](microphone.getBufferSize() / 5)

      microphone.start() // Start capturing

      while (bytesRead < 1_000_000) {
        val numBytesRead = microphone.read(data, 0, data.size)
        bytesRead += numBytesRead
        // Write the mic data to a stream for later use
        out.write(data, 0, numBytesRead)
      }
      out.close()
      microphone.stop()
      microphone.close()
      out.toByteArray()
    }
