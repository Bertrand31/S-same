package sesame

import scala.util.{Failure, Success}
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._

object Sesame extends IOApp:

  private def getMatchingSongs(footprint: ArraySeq[Long])
                              (using db: StorageHandle): IO[List[String]] =
    footprint
      .traverse(db.lookupHash)
      .map(responses =>
        responses
          .zipWithIndex
          .collect({
            case (Some((songName, songIndex)), footprintIndex) =>
              (songName, songIndex - footprintIndex)
          })
          .groupMap(_._1)(_._2)
          .toList
          .sortBy(_._2.size)
          .reverse
          .take(5)
          .map({
            case (songName, deltas) =>
              val deltaHistogram = deltas.groupMapReduce(identity)(_ => 1)(_ + _)
              val percentage = deltas.size * 100 / footprint.size.toFloat
              val roundedPct = math.round(percentage * 100) / 100F
              s"'$songName' got a $roundedPct% match. Linearity score: ${deltaHistogram.values.max}"
          })
      )

  def run(args: List[String]): IO[ExitCode] =
    for {
      given StorageHandle <- Storage.setup
      audioChunks         <- MicRecorder.recordChunks
      footprint            = SoundFootprintGenerator.transform(audioChunks)
      results             <- getMatchingSongs(footprint)
      _                   <- results match {
                               case Nil     => IO.println("NO MATCH FOUND")
                               case results => IO.println(s"\nFOUND:\n${results.mkString("\n")}\n")
                             }
    } yield ExitCode.Success

