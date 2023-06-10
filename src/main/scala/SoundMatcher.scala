package sesame

import scala.collection.immutable.ArraySeq
import cats.implicits._
import cats.effect.{IO, IOApp, ExitCode}
import sesame.storage._, aerospike.AeroClient
import sesame.types.{SongMatch, SongMetadata, SongId, TimeOffset}
import sesame.audio.MicRecorder
import sesame.footprint.SoundFootprintGenerator

object SoundMatcher extends IOApp:

  def findCorrespondingHashes(footprint: ArraySeq[Long])(
      using FootprintClient,
  ): IO[ArraySeq[(SongId, TimeOffset)]] =
    IO.parTraverseN(10)(footprint)(FootprintBridge.lookupHash).map(
      _
        .zipWithIndex
        .collect({
          case (Some((songId, indexInSong)), indexInFootprint) =>
            (songId, TimeOffset(indexInSong - indexInFootprint))
        })
    )

  def groupOffsetsBySong(
      offsets: ArraySeq[(SongId, TimeOffset)]
  ): ArraySeq[(SongId, ArraySeq[TimeOffset])] =
    offsets
      .groupMap(_._1)(_._2)
      .to(ArraySeq)

  def rankMatches(
      footprintSize: Int
  ): ArraySeq[(SongId, ArraySeq[TimeOffset])] => ArraySeq[(SongId, Float, Float)] =
    _
      .map({
        case (songId, offsets) =>
          val matchPct = offsets.size * 100 / footprintSize.toFloat
          val deltaHistogram = offsets.groupMapReduce(_.value)(_ => 1)(_ + _)
          val linearityPct = (deltaHistogram.values.max * 100) / footprintSize.toFloat
          (songId, matchPct, linearityPct)
      })
      .sortBy(- _._3)

  private val MaxResults = 5

  def formatMatches(matches: ArraySeq[(SongId, Float, Float)])(
      using MetadataClient,
  ): IO[ArraySeq[SongMatch]] =
    matches
      .take(MaxResults)
      .traverse({
        case (songId, matchPct, linearityPct) =>
          MetadataBridge.getSong(songId).flatMap({
            case None => IO.raiseError(new Error(s"No metadata for song ID ${songId.value}"))
            case Some(metadata) => IO.pure(SongMatch(songId, metadata, matchPct, linearityPct))
          })
      })

  def getMatchingSongs(footprint: ArraySeq[Long])(
      using FootprintClient, MetadataClient
  ): IO[ArraySeq[SongMatch]] =
    findCorrespondingHashes(footprint)
      .map(groupOffsetsBySong)
      .map(rankMatches(footprint.size))
      .flatMap(formatMatches)

  def run(args: List[String]): IO[ExitCode] =
    AeroClient.setup.use(aeroClient =>
      given AeroClient = aeroClient
      for {
        audioChunks      <- MicRecorder.recordChunks
        footprint        =  SoundFootprintGenerator.transform(audioChunks)
        results          <- getMatchingSongs(footprint)
        _                <- results match
                              case ArraySeq() => IO.println("NO MATCH FOUND")
                              case matches =>
                                IO.println("\nFOUND:\n") *>
                                IO.println(matches.mkString("\n") ++ "\n")
      } yield ExitCode.Success
    )

