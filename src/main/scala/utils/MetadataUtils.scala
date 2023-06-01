package sesame.utils

import java.io.File
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps
import cats.effect.IO
import org.jaudiotagger.audio.wav.WavTagReader

final case class SongMetadata(private val inner: Map[String, String]):

  val get = inner.get
  val getOrElse = inner.getOrElse
  def toMap = inner

  def getTitle: String = getOrElse(SongMetadata.SongTitleKey, "Unknown")
  def getArtist: String = getOrElse(SongMetadata.ArtistKey, "Unknown")

object SongMetadata:

  val SongTitleKey = "TITLE"
  val ArtistKey = "ARTIST"

object MetadataUtils:

  private val WavMetadataReader = new WavTagReader("")

  def getWavMetadata(audioFile: File): IO[SongMetadata] = IO {
    WavMetadataReader.read(audioFile.toPath)
      .getFields()
      .asScala
      .map(tag => (tag.getId, tag.getRawContent.map(_.toChar).mkString))
      .toMap
      .pipe(SongMetadata(_))
  }
