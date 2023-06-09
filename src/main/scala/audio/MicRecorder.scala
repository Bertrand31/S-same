package sesame

import javax.sound.sampled.AudioSystem
import cats.effect.IO
import sesame.audio.AudioCommons

object MicRecorder:

  private val NumberOfChunksToRecord = 500

  def recordChunks: IO[Iterator[Array[Byte]]] =
    IO.blocking {
      val microphone = AudioSystem.getTargetDataLine(AudioCommons.InputFormat)
      microphone.open(AudioCommons.InputFormat)
      microphone.start() // Start capturing

      val output = (0 to NumberOfChunksToRecord).toArray.map(_ =>
        val chunk = new Array[Byte](AudioCommons.ChunkSize)
        microphone.read(chunk, 0, AudioCommons.ChunkSize)
        chunk
      )

      microphone.stop()
      microphone.close()
      output.iterator
    }
