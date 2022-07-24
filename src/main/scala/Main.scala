package sesame

import javax.sound.sampled._
import java.io._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object JavaSoundRecorder {

  // Recording duration, in milliseconds
  val RECORD_TIME = 10_000L // 10 secondes

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

  def start(): Unit =
    Try {
      line = AudioSystem.getTargetDataLine(audioFormat)
      line.open(audioFormat)
      line.start() // start capturing

      println("Start capturing...")
      val ais = new AudioInputStream(line)
      println("Start recording...")
      AudioSystem.write(ais, fileType, wavFile) // start recording
    } match {
      case Failure(ex: LineUnavailableException) => ex.printStackTrace()
      case Failure(ioe: IOException) => ioe.printStackTrace()
      case _ =>
    }

  /** Closes the target data line to finish capturing and recording
    */
  def finish(): Unit =
    line.stop()
    line.close()
    println("====> Finished")
}

object Sesame extends App:

  Future {
    Thread.sleep(JavaSoundRecorder.RECORD_TIME)
  }.map(_ => JavaSoundRecorder.finish())

  // start recording
  JavaSoundRecorder.start()

