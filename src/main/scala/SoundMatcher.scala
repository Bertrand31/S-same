package sesame

import scala.util.{Failure, Success}
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._

object Sesame extends IOApp:

  private def lookupFootprint(footprint: ArraySeq[Long])
                             (using db: StorageHandle): IO[List[String]] =
    footprint
      .traverse(db.lookupHash)
      .map(
        _
          .flatten
          .groupMapReduce(identity)(_ => 1)(_ + _)
          .toList
          .sortBy(_._2)
          .reverse
          .map(tpl =>
            val percentage = tpl._2 * 100 / footprint.size.toFloat
            val roundedPct = math.round(percentage * 100) / 100F
            s"'${tpl._1}' got a $roundedPct% match"
          )
      )

  def run(args: List[String]): IO[ExitCode] =
    for {
      given StorageHandle <- Storage.setup
      audioChunks         <- MicRecorder.recordChunks
      footprint            = SoundFootprintGenerator.transform(audioChunks)
      results             <- lookupFootprint(footprint)
      _                   <- results match {
                               case Nil     => IO.println("NO MATCH FOUND")
                               case results => IO.println(s"\nFOUND:\n${results.mkString("\n")}\n")
                             }
    } yield ExitCode.Success

