package sesame.utils

import java.io.File
import scala.jdk.CollectionConverters._
import cats.effect.IO
import org.jaudiotagger.audio.wav.WavTagReader

object MetadataUtils:

  val SongTitleKey = "TITLE"
  val ArtistKey = "ARTIST"

  private val WavMetadataReader = new WavTagReader("")

  def getWavMetadata(audioFile: File): IO[Map[String, String]] = IO {
    WavMetadataReader.read(audioFile.toPath)
      .getFields()
      .asScala
      .map(tag => (tag.getId, tag.getRawContent.map(_.toChar).mkString))
      .toMap
  }
