package controllers

import javax.inject.*
import play.api.*
import play.api.mvc.*
import de.htwg.Reversi
import de.htwg.model.{Move, Stone}
import de.htwg.model.fieldComponent.FieldInterface
import play.api.libs.json.{JsValue, Json, Writes}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ReversiController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  private val gameController = Reversi.controller

  implicit val stoneWrites: Writes[Stone] = Writes[Stone] {
    case Stone.Empty => Json.toJson("EMPTY")
    case Stone.B => Json.toJson("B")
    case Stone.W => Json.toJson("W")
  }

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def game(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    print(gameController.toString)
    Ok(views.html.game(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveQuery(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val row = request.getQueryString("r").map(_.toInt).getOrElse(0)
    val column = request.getQueryString("c").map(_.toInt).getOrElse(0)

    doMove(row, column)

    Ok(views.html.game(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveSubmit(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val row = request.body.asFormUrlEncoded.flatMap(_.get("row").flatMap(_.headOption)).map(_.toInt).getOrElse(0)
    val column = request.body.asFormUrlEncoded.flatMap(_.get("column").flatMap(_.headOption)).map(_.toInt).getOrElse(0)

    doMove(row, column)

    Ok(views.html.game(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveClick(row: Int, col: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    doMove(row, col)

    Ok(views.html.game(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveAjax(row: Int, col: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val oldBoard = gameController.field
    val oldBoardJson = fieldToJson(oldBoard)
    doMove(row, col)
    val updatedBoard = gameController.field
    val boardJson = fieldToJson(updatedBoard)
    val response = Json.obj(
      "oldBoard" -> oldBoardJson,
      "newBoard" -> boardJson
    )
    Ok(response)
  }

  def rules(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.rules())
  }

  private def fieldToJson(field: FieldInterface): JsValue = {
    val fieldJson = Json.obj(
      "size" -> field.size,
      "cells" -> (0 until field.size).map { row =>
        (0 until field.size).map { col =>
          Json.toJson(field.get(row + 1, col + 1))
        }
      },
      "playerState" -> Json.toJson(gameController.playerState.getStone)
    )
    fieldJson
  }

  private def doMove(row: Int, column: Int): Unit = gameController.doAndPublish(gameController.put, Move(gameController.playerState.getStone, row, column))
}
