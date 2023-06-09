package sesame.utils

import java.io.File
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps
import cats.effect.IO
import org.jaudiotagger.audio.wav.WavTagReader
import sesame.SongMetadata

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
