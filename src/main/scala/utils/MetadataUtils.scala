package sesame.utils

import java.io.File
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps
import cats.effect.IO
import org.jaudiotagger.audio.wav.WavTagReader
import sesame.types.SongMetadata

object MetadataUtils:

  private val WavMetadataReader = new WavTagReader("")

  def getWavMetadata(audioFile: File): IO[SongMetadata] =
    IO(WavMetadataReader.read(audioFile.toPath)).map(
      _
        .getFields
        .asScala
        .map(tag => (tag.getId, new String(tag.getRawContent, StandardCharsets.UTF_8).trim))
        .toMap
        .pipe(SongMetadata(_))
    )
