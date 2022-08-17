package sesame

import java.io.ByteArrayOutputStream
import javax.sound.sampled.{AudioFormat, AudioSystem}
import cats.effect.IO

object MicRecorder:

  private val NumberOfChunksToRecord = 500

  def recordChunks: IO[Iterator[Array[Byte]]] =
    IO.blocking {
      val microphone = AudioSystem.getTargetDataLine(Commons.InputFormat)
      microphone.open(Commons.InputFormat)
      microphone.start() // Start capturing

      val output = (0 to NumberOfChunksToRecord).toArray.map(_ =>
        val chunk = new Array[Byte](Commons.ChunkSize)
        microphone.read(chunk, 0, Commons.ChunkSize)
        chunk
      )

      microphone.stop()
      microphone.close()
      output.iterator
    }
