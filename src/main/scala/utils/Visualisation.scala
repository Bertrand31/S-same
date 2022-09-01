package sesame.utils

import javax.swing._
import javax.imageio.ImageIO
import java.awt._
import java.awt.geom._
import java.awt.image.BufferedImage
import java.awt.{Graphics2D,Color,Font,BasicStroke}
import java.awt.geom._
import javax.swing.JComponent
import java.awt.image.BufferedImage
import javax.swing.JFrame
import java.awt.Color
import org.apache.commons.math3.complex.Complex

object Visualisation:

  import sesame.Commons.InputFormat

  def visualiseSpectrogram(results: Array[Array[Complex]]): Unit =
    val size = (500, 500)
    val canvas = new BufferedImage(size._1, size._2, BufferedImage.TYPE_INT_RGB)
    val g = canvas.createGraphics()
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, canvas.getWidth, canvas.getHeight)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    results.zipWithIndex.foreach({
      case (row, index) =>
        val size = row.size /// TODO ????
        var freq = 1
        for (line <- (1 to 500)) {
          // To get the magnitude of the sound at a given frequency slice
          // get the abs() from the complex number.
          // In this case I use Math.log to get a more managable number (used for color)
          val magnitude = Math.log(row(freq).abs() + 1)

          // The more blue in the color the more intensity for a given frequency point:
          g.setColor(new Color(0, (magnitude * 10).toInt, (magnitude * 20).toInt))
          // Fill:
          g.draw(new Rectangle(index % 500, (500 - line) * 500, 500, 500))

          // I used a improvised logarithmic scale and normal scale:
          if ((Math.log10(line) * Math.log10(line)) > 1) {
            freq = freq + (Math.log10(line) * Math.log10(line)).toInt
          } else {
            freq += 1
          }
        }
    })
    g.dispose()
    ImageIO.write(canvas, "png", new java.io.File("drawing.png"))
