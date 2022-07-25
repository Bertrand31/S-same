package sesame

import scala.util.Failure
import scala.util.Success
import cats.implicits._
import cats.effect._

object Sesame extends IOApp:

  private val db = Storage.setup()
  // db.store

  def run(args: List[String]): IO[ExitCode] =
    SoundRecorder.record().match {
      case Failure(err) => IO.raiseError(err)
      case Success(values) =>
        val footprint = SoundFootprintGenerator.transform(values)
        db.storeSong(footprint, "Whatev") *>
        db.lookupHash(footprint.head) >>= (
          _ match {
            case Some(foo) => IO.println(s"Success!!: $foo").as(ExitCode.Success)
            case None => IO.raiseError(new Error("Not found")).as(ExitCode.Error)
          }
        )
    }

