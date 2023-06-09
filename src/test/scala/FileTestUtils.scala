package `sesame.audio`.utils

import java.io.{File, PrintWriter}
import cats.effect._
import org.apache.commons.math3.complex.Complex

object FileTestUtils:

  def writeBytesToFile(desinationFile: File, data: Array[Byte]): IO[Unit] =
    IO.blocking {
      val pw = new PrintWriter(desinationFile)
      pw.write(data.drop(100000).take(1000).zipWithIndex.map({
        case (byte, idx) => s"$idx;$byte"
      }).mkString("\n"))
      pw.write("\n")
      pw.close
    }

  def writeComplexToFile(desinationFile: File, data: Iterator[Array[Complex]]): IO[Unit] =
    IO.blocking {
      val pw = new PrintWriter(desinationFile)
      data.take(1_000).foreach(chunk => {
        val row = chunk.take(10_000).map(_.abs()).mkString(";")
        pw.write(row ++ "\n")
      })
      pw.close
    }
