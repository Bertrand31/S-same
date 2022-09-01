package sesame

import scala.util.{Failure, Success}
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import utils.MathUtils.roundToTwoPlaces

object Sésame extends IOApp:

  def getMatchingSongs(footprint: ArraySeq[Long])(using db: StorageHandle): IO[List[String]] =
    footprint
      .traverse(db.lookupHash)
      .map(
        _
          .zipWithIndex
          .collect({
            case (Some((songName, songIndex)), footprintIndex) =>
              (songName, songIndex - footprintIndex)
          })
          .groupMap(_._1)(_._2)
          .toList
          .map({
            case (songName, deltas) =>
              val deltaHistogram = deltas.groupMapReduce(identity)(_ => 1)(_ + _)
              val matchPct = deltas.size * 100 / footprint.size.toFloat
              val linearityPct = (deltaHistogram.values.max * 100) / footprint.size.toFloat
              (songName, matchPct, linearityPct)
          })
          .sortBy(_._3)
          .reverse
          .take(5)
          .map({
            case (songName, matchPct, linearityPct) =>
              s"==> $songName <==".padTo(30, '=') ++ "\n" ++
              s"- Hashes matching at ${roundToTwoPlaces(matchPct)}%\n" ++
              s"- Linearity matching at: ${roundToTwoPlaces(linearityPct)}%\n"
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

