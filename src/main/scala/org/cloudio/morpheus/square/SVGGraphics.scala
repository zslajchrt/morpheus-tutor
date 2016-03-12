package org.cloudio.morpheus.square

import java.io.PrintWriter

/**
  * Created by zslajchrt on 12/03/16.
  */
class SVGGraphics(out: PrintWriter) extends Graphics {

  def start(): Unit = {
    out.println("<svg xmlns='http://www.w3.org/2000/svg' viewBox='-50 -50 500 500'>")
  }

  def end(): Unit = {
    out.println("</svg>")
  }

  override def drawRect(x: Double, y: Double, w: Double, h: Double): Unit = {
//      <rect width="300" height="100" style="fill:rgb(0,0,255);stroke-width:3;stroke:rgb(0,0,0)" />
    //println(s"<rect x=\"$x\" y=\"$y\" width=\"$w\" height=\"$h\"/>")
    out.println(s"<rect x='$x' y='$y' width='$w' height='$h' stroke='blue' fill='purple' fill-opacity='0' stroke-opacity='1'/>")
  }

  override def drawCircle(x: Double, y: Double, r: Double): Unit = {
    out.println(s"<circle cx='$x' cy='$y' r='$r' stroke='blue' fill='purple' fill-opacity='0' stroke-opacity='1'/>")
  }
}
