package sesame

import javax.sound.sampled.{AudioFormat, AudioFileFormat, AudioSystem}
import java.io.{ByteArrayOutputStream, File}
import cats.effect._

object MicRecorder:

  private val audioFormat =
    val sampleRate = 8000.0F
    val sampleSizeInBits = 16
    val channels = 1
    val signed = true
    val bigEndian = true
    AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)

  def record: IO[Array[Byte]] =
    IO {
      val microphone = AudioSystem.getTargetDataLine(audioFormat)
      val out = new ByteArrayOutputStream()
      microphone.open(audioFormat)
      var bytesRead = 0
      var numBytesRead = 0
      val ChunkSize = 1024
      val data = new Array[Byte](microphone.getBufferSize() / 5)

      microphone.start() // Start capturing

      while (bytesRead < 200000) {
        numBytesRead = microphone.read(data, 0, ChunkSize)
        bytesRead += numBytesRead
        // write the mic data to a stream for later use
        out.write(data, 0, numBytesRead)
      }
      out.close()
      microphone.stop()
      microphone.close()
      val byteArray = out.toByteArray()
      println(s"Mic recording length: ${byteArray.size}")
      byteArray
    }
