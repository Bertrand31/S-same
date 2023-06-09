package sesame.utils

object MathUtils:

  def round(places: Int)(number: Double): Double =
    val powerOf10 = math.pow(10, places)
    math.round(number * powerOf10) / powerOf10.toDouble

  val roundToTwoPlaces: Double => Double = round(2)

  def toPaddedBytesArray(nb: Int|Short): Array[Byte] =
    val (bigInt, bytes) =
      nb match
        case n: Int => (BigInt(n), 4)
        case n: Short => (BigInt(n), 2)
    bigInt.toByteArray.reverse.take(bytes).padTo(bytes, 0.toByte).reverse

  def byteArrayToInt(arr: Array[Byte]): Int =
    BigInt(arr.reverse.take(4).reverse).toInt

  def byteArrayToShort(arr: Array[Byte]): Short =
    BigInt(arr.reverse.take(2).reverse).toShort
