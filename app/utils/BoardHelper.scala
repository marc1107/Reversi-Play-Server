package utils

import de.htwg.model.Stone
import de.htwg.model.fieldComponent.FieldInterface

object BoardHelper {
  private var previousBoard: Option[FieldInterface] = None

  def boardToHtml(board: FieldInterface): String = {
    val tableContent = for {
      row <- 0 until board.size
    } yield {
      val rowContent = for {
        cell <- 0 until board.size
      } yield {
        val stone: Stone = board.get(row + 1, cell + 1)
        val previousStone: Option[Stone] = previousBoard.map(_.get(row + 1, cell + 1))
        val content = (stone, previousStone) match {
          case (Stone.B, Some(Stone.W)) => "<div class='stone white flip-to-black'></div>"
          case (Stone.W, Some(Stone.B)) => "<div class='stone black flip-to-white'></div>"
          case (Stone.W, _) => "<div class='stone white'></div>"
          case (Stone.B, _) => "<div class='stone black'></div>"
          case (Stone.Empty, _) => "&nbsp;"
        }
        s"<td>$content</td>"
      }
      s"<tr>${rowContent.mkString}</tr>"
    }
    previousBoard = Some(board) // Update the previous board state
    s"""<table class="board">${tableContent.mkString}</table>"""
  }
}