package sesame.utils

object MathUtils:

  def round(places: Int, number: Float): Float =
    val powerOf10 = math.pow(10, places)
    math.round(number * powerOf10) / powerOf10.toFloat

