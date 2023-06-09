package sesame.utils

import java.io.{File, FileNotFoundException}
import cats.effect.IO

object FileUtils:

  def getFilesIn(path: String): IO[List[File]] =
    val dir = new File(path)
    if (!dir.exists)
      IO.raiseError(new FileNotFoundException(s"$dir does not exist"))
    else if (!dir.isDirectory)
      IO.raiseError(new FileNotFoundException(s"$dir is not a directory"))
    else
      IO { dir.listFiles.filter(_.isFile).toList }
