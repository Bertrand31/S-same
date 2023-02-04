package sesame.utils

object MathUtils:

  def round(places: Int)(number: Double): Double =
    val powerOf10 = math.pow(10, places)
    math.round(number * powerOf10) / powerOf10.toDouble

  val roundToTwoPlaces: Double => Double = round(2)

  def intToByteArray(int: Int): Array[Byte] =
    BigInt(int).toByteArray.reverse.take(4).padTo(4, 0.toByte).reverse

  def byteArrayToInt(arr: Array[Byte]): Int =
    BigInt(arr.reverse.take(4).reverse).toInt

  def shortToByteArray(short: Short): Array[Byte] =
    BigInt(short).toByteArray.reverse.take(2).padTo(2, 0.toByte).reverse

  def byteArrayToShort(arr: Array[Byte]): Short =
    BigInt(arr.reverse.take(2).reverse).toShort
