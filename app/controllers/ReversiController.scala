package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import de.htwg.Reversi
import de.htwg.model.Move

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

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    print(gameController.toString)
    Ok(views.html.index(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveQuery(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val row = request.getQueryString("r").map(_.toInt).getOrElse(0)
    val column = request.getQueryString("c").map(_.toInt).getOrElse(0)

    doMove(row, column)

    Ok(views.html.index(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveSubmit(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val row = request.body.asFormUrlEncoded.flatMap(_.get("row").flatMap(_.headOption)).map(_.toInt).getOrElse(0)
    val column = request.body.asFormUrlEncoded.flatMap(_.get("column").flatMap(_.headOption)).map(_.toInt).getOrElse(0)

    doMove(row, column)

    Ok(views.html.index(gameController.field, gameController.playerState.getStone))
  }

  def makeMoveClick(row: Int, col: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    doMove(row, col)

    Ok(views.html.index(gameController.field, gameController.playerState.getStone))
  }

  def rules(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.rules())
  }

  private def doMove(row: Int, column: Int): Unit = gameController.doAndPublish(gameController.put, Move(gameController.playerState.getStone, row, column))
}
