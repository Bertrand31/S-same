package sesame.types

final case class SongMetadata(private val inner: Map[String, String]):

  val get = inner.get
  val getOrElse = inner.getOrElse
  def toMap = inner

  def getTitle: String = getOrElse(SongMetadata.SongTitleKey, "Unknown")
  def getArtist: String = getOrElse(SongMetadata.ArtistKey, "Unknown")

object SongMetadata:

  val SongTitleKey = "TITLE"
  val ArtistKey = "ARTIST"
