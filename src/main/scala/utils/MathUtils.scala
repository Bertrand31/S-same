package sesame.utils

object MathUtils:

  def round(places: Int)(number: Double): Double =
    val powerOf10 = math.pow(10, places)
    math.round(number * powerOf10) / powerOf10.toDouble

  val roundToTwoPlaces: Double => Double = round(2)

  def longToByteArray(long: Long): Array[Byte] =
    BigInt(long).toByteArray.reverse.take(8).padTo(8, 0.toByte).reverse

  def byteArrayToLong(arr: Array[Byte]): Long =
    BigInt(arr.take(8)).toLong

  def charToByteArray(char: Char): Array[Byte] =
    BigInt(char).toByteArray.reverse.take(2).padTo(2, 0.toByte).reverse

  def byteArrayToChar(arr: Array[Byte]): Char =
    BigInt(arr.take(8)).toChar
