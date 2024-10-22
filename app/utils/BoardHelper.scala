package utils

import de.htwg.model.Stone
import de.htwg.model.fieldComponent.FieldInterface

object BoardHelper {
  def boardToHtml(board: FieldInterface): String = {
    val tableContent = for {
      row <- 0 until board.size
    } yield {
      val rowContent = for {
        cell <- 0 until board.size
      } yield {
        val stone: Stone = board.get(row + 1, cell + 1)
        val content = stone match {
          case Stone.W => "<div class='stone white'></div>"
          case Stone.B => "<div class='stone black'></div>"
          case Stone.Empty => "&nbsp;"
        }
        s"<td>$content</td>"
      }
      s"<tr>${rowContent.mkString}</tr>"
    }
    s"""<table class="board">${tableContent.mkString}</table>"""
  }
}