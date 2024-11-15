package controllers

import javax.inject.*
import play.api.mvc.*
import de.htwg.Reversi
import de.htwg.model.{Move, Stone}
import de.htwg.model.fieldComponent.FieldInterface
import org.apache.pekko.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.pekko.stream.Materializer
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.streams.ActorFlow

import scala.swing.Reactor

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ReversiController @Inject()(val controllerComponents: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends BaseController {

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

  def makeMoveAjax(row: Int, col: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val oldBoard = gameController.field
    val oldBoardJson = fieldToJson(oldBoard)
    doMove(row, col)
    val updatedBoard = gameController.field
    println("Updated board: " + updatedBoard.toString)
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


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      println("Connect received")
      ReversiWebSocketActorFactory.create(out)
    }
  }

  object ReversiWebSocketActorFactory {
    def create(out: ActorRef): Props = {
      Props(new ReversiWebSocketActor(out))
    }
  }

  class ReversiWebSocketActor(out: ActorRef) extends Actor with Reactor {

    def receive = {
      case msg: String =>
        println("Received message from client")
        val json = Json.parse(msg)
        val row = (json \ "row").as[Int]
        val col = (json \ "col").as[Int]
        doMove(row, col)
        out ! fieldToJson(gameController.field).toString
    }
  }
}
