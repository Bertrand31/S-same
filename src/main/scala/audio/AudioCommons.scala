package sesame.audio

import javax.sound.sampled.AudioFormat

object AudioCommons:

  val ChunkSize = 4096

  val InputFormat =
    val sampleRate = 44100F
    val sampleSizeInBits = 16
    val channels = 1
    val signed = true
    val bigEndian = false
    AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)

final case class AudioFormatException(
  private val message: String = "File format didn't match the expected one",
  private val cause: Throwable = None.orNull
) extends Exception(message, cause)
