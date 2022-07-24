package sesame

import javax.sound.sampled._
import java.io._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

final case class RecorderHandle(private val line: TargetDataLine) {

  /** Closes the target data line to finish capturing and recording
    */
  def finish(): Unit =
    line.stop()
    line.close()
    println("====> Finished")
}

object JavaSoundRecorder {

  private val wavFile = new File("./foo.wav")
  private val fileType = AudioFileFormat.Type.WAVE
  private var line: TargetDataLine = null

  /** Defines an audio format
    */
  private val audioFormat =
    val sampleRate = 16000F
    val sampleSizeInBits = 8
    val channels = 2
    val signed = true
    val bigEndian = true
    new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)

  def start(): RecorderHandle =
    line = AudioSystem.getTargetDataLine(audioFormat)
    Future { // Fire and forget
      Try {
        line.open(audioFormat)
        line.start() // Start capturing

        println("Start capturing...")
        val ais = new AudioInputStream(line)
        println("Start recording...")
        AudioSystem.write(ais, fileType, wavFile) // start recording
      } match {
        case Failure(ex: LineUnavailableException) => ex.printStackTrace()
        case Failure(ioe: IOException) => ioe.printStackTrace()
        case _ =>
      }
    }
    RecorderHandle(line)
}

object Sesame extends App:

  // Recording duration, in milliseconds
  private val RecordingTime = 10_000 // 10 secondes

  // Start recording
  private val handle = JavaSoundRecorder.start()

  Thread.sleep(RecordingTime)
  handle.finish()

