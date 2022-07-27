package sesame

import scala.util.{Failure, Success}
import cats.implicits._
import cats.effect._

object Sesame extends IOApp:

  private def lookupFootprint(footprint: Array[Long])
                             (implicit db: StorageHandle): IO[List[String]] =
    footprint
      .toList
      .traverse(db.lookupHash)
      .map(
        _
          .flatten
          .groupMapReduce(identity)(_ => 1)(_ + _)
          .map(tpl =>
            val percentage = tpl._2 * 100 / footprint.size
            s"'${tpl._1}' got a $percentage% match"
          )
          .toList
      )

  def run(args: List[String]): IO[ExitCode] =
    for {
      db       <- Storage.setup
      values   <- MicRecorder.record
      footprint = SoundFootprintGenerator.transform(values)
      _ = println("Recorded:")
      _ = println(footprint.toList)
      results  <- lookupFootprint(footprint)(db)
      _        <- results match {
                    case Nil     => IO.println("NO MATCH FOUND")
                    case results => IO.println(s"\nFOUND:\n${results.mkString("\n")}\n=========\n")
                  }
    } yield ExitCode.Success

