package sesame.utils

import java.io.File
import java.nio.file.{Files, Paths}
import cats.effect._
import org.apache.commons.math3.complex.Complex

object FileUtils:

  def getFilesIn(dir: File): IO[List[File]] =
    if (!dir.exists)
      IO.raiseError(new Exception(s"$dir does not exist"))
    else if (!dir.isDirectory)
      IO.raiseError(new Exception(s"$dir is not a directory"))
    else
      IO { dir.listFiles.filter(_.isFile).toList }

  import java.io._

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
