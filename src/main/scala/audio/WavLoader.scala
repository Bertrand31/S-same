package sesame

import java.io.File
import javax.sound.sampled.AudioSystem
import cats.effect.IO
import sesame.audio.{AudioCommons, AudioFormatException}

object WavLoader:

  def wavToByteChunks(wavFile: File): IO[Iterator[Array[Byte]]] =
    IO { AudioSystem.getAudioFileFormat(wavFile) }.flatMap({
      case fileFormat if fileFormat.getFormat matches AudioCommons.InputFormat =>
        IO.blocking {
          val audioInputStream = AudioSystem.getAudioInputStream(wavFile)
          val bytesNumber = fileFormat.getByteLength()

          Iterator.fill(bytesNumber / AudioCommons.ChunkSize) {
            val chunk = new Array[Byte](AudioCommons.ChunkSize)
            audioInputStream.read(chunk, 0, AudioCommons.ChunkSize)
            chunk
          }
        }
      case fileFormat =>
        IO.raiseError(AudioFormatException(s"$fileFormat did not match ${AudioCommons.InputFormat}"))
    })
