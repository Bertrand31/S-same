package sesame.footprint

import scala.collection.immutable.ArraySeq
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}

object SoundFootprintGenerator:

  private val transformer = new FastFourierTransformer(DftNormalization.STANDARD)

  val toFourier: Iterator[Array[Byte]] => Iterator[Array[Complex]] =
    _
      .map(_.map(_.toDouble))
      .map(transformer.transform(_, TransformType.FORWARD)) // Perform FFT analysis on the chunk

  private val LowerLimit = 40
  private val UpperLimit = 300

  private val Range = Array(LowerLimit, 80, 120, 180, UpperLimit)

  private val FuzFactor = 2

  private def hash(p1: Long, p2: Long, p3: Long, p4: Long): Long =
    (p4 - (p4 % FuzFactor)) * 10_000_000 +
    (p3 - (p3 % FuzFactor)) * 10_000 +
    (p2 - (p2 % FuzFactor)) * 100 +
    (p1 - (p1 % FuzFactor))

  private def getIndex(freq: Int): Int =
    Range.indexWhere(_ >= freq)

  private val hashKeyPoints: Iterator[Array[Complex]] => ArraySeq[Long] =
    _.map(chunk =>
      val highscores = new Array[Double](Range.size - 1)
      val recordPoints = new Array[Long](Range.size - 1)
      ((LowerLimit + 1) to (UpperLimit)).foreach(freq =>
        val mag = math.log(chunk(freq).abs + 1) // Get the magnitude
        val index = getIndex(freq) - 1 // Find out which range we are in
        // Save the highest magnitude and corresponding frequency
        if (mag > highscores(index)) {
          highscores.update(index, mag)
          recordPoints.update(index, freq)
        }
      )
      hash(recordPoints(0), recordPoints(1), recordPoints(2), recordPoints(3))
    ).to(ArraySeq)

  val transform: Iterator[Array[Byte]] => ArraySeq[Long] =
    toFourier andThen hashKeyPoints
