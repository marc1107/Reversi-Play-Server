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
import utils.{ChatStorage, GameState}

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
  private val baseUrl = "https://reversi-vue-6c1a84673c8d.herokuapp.com"
  //private val baseUrl = "http://localhost:3000"

  implicit val stoneWrites: Writes[Stone] = Writes[Stone] {
    case Stone.Empty => Json.toJson("EMPTY")
    case Stone.B => Json.toJson("B")
    case Stone.W => Json.toJson("W")
  }

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (GameState.isGameFull) {
      println("Game is full")
      Forbidden("The game already has two players.")
    } else if (GameState.connectedPlayers.isEmpty) {
      println("No players connected")
      Ok(views.html.index())
    } else {
      println("Player connected")
      Redirect(routes.ReversiController.joinGame())
    }
  }

  def joinGame(): Action[AnyContent] = Action { implicit request =>
    val session = request.session

    // Check if the player already has a session
    val playerSession = session.get("player")

    playerSession match {
      case Some(player) =>
        if (!GameState.connectedPlayers.contains(player)) {
          GameState.addPlayer(player)
        }
        // Player is reconnecting, just return them to the game
        Ok(views.html.game(gameController.field, gameController.playerState.getStone))
          .withSession(session + ("player" -> player))

      case None =>
        // Assign a new session based on how many players are currently in the game
        val newPlayerId = if (GameState.connectedPlayers.isEmpty) "player_1" else "player_2"

        if (GameState.addPlayer(newPlayerId)) {
          Ok(views.html.game(gameController.field, gameController.playerState.getStone))
            .withSession(request.session + ("player" -> newPlayerId))
        } else {
          Forbidden("The game already has two players.")
        }
    }
  }

  def game(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
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
    println(s"makeMoveAjax called with row: $row, col: $col")
    val origin = request.headers.get("Origin").getOrElse(baseUrl)
    val oldBoard = gameController.field
    val oldBoardJson = fieldToJson(oldBoard)
    doMove(row, col)
    val updatedBoard = gameController.field
    val boardJson = fieldToJson(updatedBoard)
    val response = Json.obj(
      "oldBoard" -> oldBoardJson,
      "newBoard" -> boardJson
    )

    // Antwort mit CORS-Headern zurückgeben
    Ok(response).withHeaders(
      "Access-Control-Allow-Origin" -> origin, // Erlaubt Anfragen vom Frontend
      "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS", // Erlaubte Methoden
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With" // Erlaubte Header
    )
  }

  def newGame(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val origin = request.headers.get("Origin").getOrElse(baseUrl)
    val oldBoard = gameController.field
    val oldBoardJson = fieldToJson(oldBoard)
    gameController.newGame()
    val updatedBoard = gameController.field
    val boardJson = fieldToJson(updatedBoard)
    val response = Json.obj(
      "oldBoard" -> oldBoardJson,
      "newBoard" -> boardJson
    )
    Ok(response).withHeaders(
      "Access-Control-Allow-Origin" -> origin, // Erlaubt Anfragen vom Frontend
      "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS", // Erlaubte Methoden
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With" // Erlaubte Header
    )
  }

  def getField(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val origin = request.headers.get("Origin").getOrElse(baseUrl)
    val field = gameController.field
    val response = Json.obj(
      "newBoard" -> fieldToJson(field)
    )
    Ok(response).withHeaders(
      "Access-Control-Allow-Origin" -> origin, // Erlaubt Anfragen vom Frontend
      "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS", // Erlaubte Methoden
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With" // Erlaubte Header
    )
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

  def getPlayerNames: Action[AnyContent] = Action { request =>
    val origin = request.headers.get("Origin").getOrElse(baseUrl)
    val playerNames = Json.obj(
      "player1Name" -> GameState.playerNames("player_1"),
      "player2Name" -> GameState.playerNames("player_2")
    )
    Ok(playerNames).withHeaders(
      "Access-Control-Allow-Origin" -> origin, // Erlaubt Anfragen vom Frontend
      "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS", // Erlaubte Methoden
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With" // Erlaubte Header
    )
  }

  def setPlayerNames(player1: String, player2: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    println(s"setPlayerNames called with player1: $player1, player2: $player2")
    GameState.changePlayerNames(player1, player2)

    val origin = request.headers.get("Origin").getOrElse(baseUrl)
    // Antwort mit CORS-Headern zurückgeben
    Ok("Player names changed").withHeaders(
      "Access-Control-Allow-Origin" -> origin, // Erlaubt Anfragen vom Frontend
      "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS", // Erlaubte Methoden
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With" // Erlaubte Header
    )
  }


  private def doMove(row: Int, column: Int): Unit = gameController.doAndPublish(gameController.put, Move(gameController.playerState.getStone, row, column))


  def socket = WebSocket.accept[String, String] { request =>
    val session = request.session
    ActorFlow.actorRef { out =>
      println("Connect received")
      ReversiWebSocketActorFactory.create(out, session)
    }
  }

  object ReversiWebSocketActorFactory {
    def create(out: ActorRef, session: Session): Props = {
      GameState.addConnection(out)
      Props(new ReversiWebSocketActor(out, session))
    }
  }

  class ReversiWebSocketActor(out: ActorRef, session: Session) extends Actor {

    def receive = {
      case msg: String =>
        println("Received message from client")
        val json = Json.parse(msg)
        val row = (json \ "row").as[Int]
        val col = (json \ "col").as[Int]

        // Check if the current player is either player_1 or player_2
        val currentPlayer = session.get("player").getOrElse("")
        if (currentPlayer != "player_1" && currentPlayer != "player_2" || !GameState.isPlayerTurn(currentPlayer)) {
          println("It's not " + currentPlayer + "'s turn")
        } else {
          doMove(row, col)

          // message: ${playerName} hat einen Zug auf [${row}, ${col}] gemacht.
          ChatStorage.addMessage(GameState.playerNames(currentPlayer) + " hat einen Zug auf [" + row + ", " + col + "] gemacht.")

          gameController.playerState.getStone match {
            case Stone.B => GameState.switchTurn("player_1")
            case Stone.W => GameState.switchTurn("player_2")
            case _ => println("Invalid player state")
          }
          GameState.broadcast(fieldToJson(gameController.field).toString)
        }
    }
  }
}
