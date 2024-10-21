package utils

object BoardHelper {
  def boardToHtml(board: String): String = {
    val rows = board.split("\n").filter(_.contains("|"))
    val tableRows = rows.map { row =>
      val cells = row.split("\\|").tail.init.map { cell =>
        val content = if (cell.trim.isEmpty) "&nbsp;" else cell.trim
        s"<td>$content</td>"
      }.mkString
      s"<tr>$cells</tr>"
    }.mkString
    s"""<table class="board">$tableRows</table>"""
  }
}