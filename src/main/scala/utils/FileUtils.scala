package sesame.utils

import java.io.File
import java.nio.file.{Files, Paths}
import cats.effect._

object FileUtils:

  def getFilesIn(dir: File): IO[List[File]] =
    if (!dir.exists) IO.raiseError(new Exception(s"$dir does not exist"))
    else if (!dir.isDirectory) IO.raiseError(new Exception(s"$dir is not a directory"))
    else IO { dir.listFiles.filter(_.isFile).toList }
