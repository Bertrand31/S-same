package sesame

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}

object SoundFootprintGenerator:

  private def toFourier(audio: Array[Byte]): Array[Array[Complex]] =
    val totalSize = audio.size
    val ChunkSize = 1024
    val amountPossible = totalSize / ChunkSize

    val transformer = new FastFourierTransformer(DftNormalization.STANDARD)

    val results = new Array[Array[Complex]](amountPossible)
    for (times <- 0 until amountPossible) {
      val complex: Array[Complex] = new Array[Complex](ChunkSize)
      for (i <- 0 until ChunkSize) {
        // Put the time domain data into a complex number with imaginary part as 0:
        complex.update(i, new Complex(audio((times * ChunkSize) + i), 0))
      }
      // Perform FFT analysis on the chunk:
      results.update(times, transformer.transform(complex, TransformType.FORWARD))
    }
    results

  private val LowerLimit = 40
  private val UpperLimit = 300

  private val Range = Array(LowerLimit, 80, 120, 180, UpperLimit)

  private val FuzFactor = 2

  private def hash(p1: Long, p2: Long, p3: Long, p4: Long): Long =
    (p4 - (p4 % FuzFactor)) * 100000000 + (p3 - (p3 % FuzFactor))
      * 100000 + (p2 - (p2 % FuzFactor)) * 100
      + (p1 - (p1 % FuzFactor))

  // Find out in which range
  private def getIndex(freq: Int): Int = {
    var i = 0
    while (Range(i) < freq) i += 1
    i
  }

  private def hashKeyPoints(results: Array[Array[Complex]]): Array[Long] =
    results.map(row => {
      val highscores = new Array[Double](Range.size)
      val recordPoints = new Array[Long](Range.size)
      for (freq <- LowerLimit until (UpperLimit - 1)) {
        //Get the magnitude:
        val mag = math.log(row(freq).abs() + 1)
        //Find out which range we are in:
        val index = getIndex(freq)
        // Save the highest magnitude and corresponding frequency:
        if (mag > highscores(index)) {
          highscores.update(index, mag)
          recordPoints.update(index, freq)
        }
      }
      hash(recordPoints(0), recordPoints(1), recordPoints(2), recordPoints(3))
    })


  def transform: Array[Byte] => Array[Long] =
    toFourier andThen hashKeyPoints
