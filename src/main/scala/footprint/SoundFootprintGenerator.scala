package sesame

import scala.collection.immutable.ArraySeq
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}
import scala.util.hashing.MurmurHash3

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

  private def hash(peaks: Array[Long]): Long =
    MurmurHash3.stringHash(peaks.mkString("|"))

  private def getIndex(freq: Int): Int =
    Range.indexWhere(_ >= freq)

  private val hashKeyPoints: Iterator[Array[Complex]] => ArraySeq[Long] =
    _.map(row =>
      val highscores = new Array[Double](Range.size)
      val recordPoints = new Array[Long](Range.size)
      (0 to (UpperLimit - 1)).foreach(freq =>
        val mag = math.log(row(freq).abs + 1) // Get the magnitude
        val index = getIndex(freq) // Find out which range we are in
        // Save the highest magnitude and corresponding frequency
        if (mag > highscores(index)) {
          highscores.update(index, mag)
          recordPoints.update(index, freq)
        }
      )
      // WARNING: discarding last point
      hash(recordPoints.init)
    ).to(ArraySeq)

  val transform: Iterator[Array[Byte]] => ArraySeq[Long] =
    toFourier andThen hashKeyPoints
