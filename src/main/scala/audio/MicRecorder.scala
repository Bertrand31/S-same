package sesame.audio

import javax.sound.sampled.AudioSystem
import cats.effect.IO

object MicRecorder:

  private val NumberOfChunksToRecord = 500 // About 23 seconds worth of data

  def recordChunks: IO[Iterator[Array[Byte]]] =
    IO.blocking {
      val microphone = AudioSystem.getTargetDataLine(AudioCommons.InputFormat)
      microphone.open(AudioCommons.InputFormat)
      microphone.start() // Start capturing

      val output = (0 until NumberOfChunksToRecord).toArray.map(_ =>
        val chunk = new Array[Byte](AudioCommons.ChunkSize)
        microphone.read(chunk, 0, AudioCommons.ChunkSize)
        chunk
      )

      microphone.stop()
      microphone.close()
      output.iterator
    }
