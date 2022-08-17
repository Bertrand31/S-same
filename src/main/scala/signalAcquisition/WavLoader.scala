package sesame

import java.io.File
import javax.sound.sampled.AudioSystem
import cats.effect.IO

object WavLoader:

  def wavToByteChunks(wavFile: File): IO[Iterator[Array[Byte]]] =
    IO { AudioSystem.getAudioFileFormat(wavFile) }.flatMap({
      case fileFormat if fileFormat.getFormat matches Commons.InputFormat =>
        IO.blocking {
          val audioInputStream = AudioSystem.getAudioInputStream(wavFile)
          val bytesNumber = fileFormat.getByteLength()

          Iterator.fill(bytesNumber / Commons.ChunkSize) {
            val chunk = new Array[Byte](Commons.ChunkSize)
            audioInputStream.read(chunk, 0, Commons.ChunkSize)
            chunk
          }
        }
      case fileFormat =>
        IO.raiseError(AudioFormatException(s"$fileFormat did not match ${Commons.InputFormat}"))
    })
