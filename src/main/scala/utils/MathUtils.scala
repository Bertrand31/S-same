package sesame.utils

object MathUtils:

  def round(places: Int)(number: Double): Double =
    val powerOf10 = math.pow(10, places)
    math.round(number * powerOf10) / powerOf10.toDouble

  val roundToTwoPlaces: Double => Double = round(2)

