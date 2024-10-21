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
        val content = if (stone == Stone.Empty) "&nbsp;" else stone.toString
        s"<td>$content</td>"
      }
      s"<tr>${rowContent.mkString}</tr>"
    }
    s"""<table class="board">${tableContent.mkString}</table>"""
  }
}