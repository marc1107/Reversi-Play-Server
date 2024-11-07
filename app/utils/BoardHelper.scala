package utils

import de.htwg.model.Stone
import de.htwg.model.fieldComponent.{Field, FieldInterface}
import play.api.libs.json.JsValue

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
        //s"""<td data-row="${row+1}" data-cell="${cell+1}">$content</td>"""
        s"""<td data-row="${row+1}" data-cell="${cell+1}" onclick="makeMove(${row+1}, ${cell+1})">$content</td>"""
      }
      s"<tr>${rowContent.mkString}</tr>"
    }
    previousBoard = Some(board) // Update the previous board state
    s"""<table class="board">${tableContent.mkString}</table>"""
  }

  private def jsonToField(json: JsValue): FieldInterface = {
    val size = (json \ "size").as[Int]
    val cells = (json \ "cells").as[Array[Array[Array[String]]]]
    val field = new Field(size, Stone.Empty)
    for {
      row <- 0 until size
      col <- 0 until size
    } {
      val stone = cells(0)(row)(col) match {
        case "B" => Stone.B
        case "W" => Stone.W
        case _ => Stone.Empty
      }
      field.put(stone, row + 1, col + 1)
    }
    field
  }
  
  def boardToHtml(json: JsValue): String = {
    val field = jsonToField(json)
    boardToHtml(field)
  }
}