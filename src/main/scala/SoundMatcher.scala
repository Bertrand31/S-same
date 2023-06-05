package sesame

import scala.util.{Failure, Success}
import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect._
import utils.SongMetadata
import utils.MathUtils.roundToTwoPlaces

object Sésame extends IOApp:

  final case class SongMatch(
    songId: Int,
    songData: SongMetadata,
    matchPercentage: Float,
    linearityMatchPercentage: Float,
  )

  def findCorrespondingHashes(footprint: ArraySeq[Long])(
      using footprintDB: FootprintDB,
  ): IO[ArraySeq[(Int, Int)]] =
    IO.parTraverseN(10)(footprint)(footprintDB.lookupHash).map(
      _
        .zipWithIndex
        .collect({
          case (Some((indexInSong, songId)), indexInFootprint) =>
            (songId, indexInSong - indexInFootprint)
        })
    )

  val groupOffsetsBySong: ArraySeq[(Int, Int)] => ArraySeq[(Int, ArraySeq[Int])] =
    _
      .groupMap(_._1)(_._2)
      .to(ArraySeq)

  def rankMatches(footprintSize: Int): ArraySeq[(Int, ArraySeq[Int])] => ArraySeq[(Int, Float, Float)] =
    _
      .map({
        case (songId, deltas) =>
          val matchPct = deltas.size * 100 / footprintSize.toFloat
          val deltaHistogram = deltas.groupMapReduce(identity)(_ => 1)(_ + _)
          val linearityPct = (deltaHistogram.values.max * 100) / footprintSize.toFloat
          (songId, matchPct, linearityPct)
      })
      .sortBy(- _._3)

  private val MaxResults = 5

  def formatMatches(matches: ArraySeq[(Int, Float, Float)])(using metadataDB: MetadataDB): IO[ArraySeq[SongMatch]] =
    matches
      .take(MaxResults)
      .traverse({
        case (songId, matchPct, linearityPct) =>
          metadataDB.getSong(songId).flatMap({
            case None => IO.raiseError(new Error(s"No metadata for song ID $songId"))
            case Some(metadata) => IO.pure(SongMatch(songId, metadata, matchPct, linearityPct))
          })
      })

  def getMatchingSongs(footprint: ArraySeq[Long])(
      using footprintDB: FootprintDB,
      metadataDB: MetadataDB,
  ): IO[ArraySeq[SongMatch]] =
    findCorrespondingHashes(footprint)
      .map(groupOffsetsBySong)
      .map(rankMatches(footprint.size))
      .flatMap(formatMatches)

  private def formatMatch(matchingSong: SongMatch): String =
    val songName = matchingSong.songData.getTitle
    val artist = matchingSong.songData.getArtist
    s"==> $artist - $songName <==".padTo(30, '=') ++ "\n" ++
    s"- Hashes matching at ${roundToTwoPlaces(matchingSong.matchPercentage)}%\n" ++
    s"- Linearity matching at: ${roundToTwoPlaces(matchingSong.linearityMatchPercentage)}%\n"

  def run(args: List[String]): IO[ExitCode] =
    for {
      databaseHandles <- Storage.setup
      (given FootprintDB, given MetadataDB) = databaseHandles
      audioChunks     <- MicRecorder.recordChunks
      footprint       =  SoundFootprintGenerator.transform(audioChunks)
      results         <- getMatchingSongs(footprint)
      _               <- results match
                           case ArraySeq() => IO.println("NO MATCH FOUND")
                           case matches =>
                             IO.println("\nFOUND:\n") *>
                             IO.println(matches.map(formatMatch).mkString("\n") ++ "\n")
    } yield ExitCode.Success

