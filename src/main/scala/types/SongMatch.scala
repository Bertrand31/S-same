package sesame

final case class SongMatch(
  songId: SongId,
  songData: SongMetadata,
  matchPercentage: Float,
  linearityMatchPercentage: Float,
)
