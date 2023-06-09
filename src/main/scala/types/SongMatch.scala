package sesame

import utils.MathUtils.roundToTwoPlaces

final case class SongMatch(
  songId: SongId,
  songData: SongMetadata,
  matchPercentage: Float,
  linearityMatchPercentage: Float,
) {

  override def toString(): String =
    s"==> ${songData.getArtist} - ${songData.getTitle} <==".padTo(30, '=') ++ "\n" ++
    s"- Hashes matching at ${roundToTwoPlaces(matchPercentage)}%\n" ++
    s"- Linearity matching at: ${roundToTwoPlaces(linearityMatchPercentage)}%\n"
}
