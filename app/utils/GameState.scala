package utils

import org.apache.pekko.actor.ActorRef

object GameState {
  var connectedPlayers: Set[String] = Set()
  var webSocketConnections: Set[ActorRef] = Set()
  var currentPlayerTurn: Option[String] = Some("player_1") // Track whose turn it is (player_1 or player_2)

  def addPlayer(playerId: String): Boolean = {
    if (connectedPlayers.size < 2) {
      connectedPlayers += playerId
      true
    } else {
      false // Game already has two players
    }
  }

  def removePlayer(playerId: String): Unit = {
    connectedPlayers -= playerId
  }

  def isGameFull: Boolean = connectedPlayers.size >= 2

  def isPlayerTurn(playerId: String): Boolean = {
    currentPlayerTurn.contains(playerId)
  }

  def switchTurn(turn: String): Unit = {
    // default player1 if no player2 or no player is connected
    currentPlayerTurn = if (connectedPlayers.contains("player_2")) {
      Some(turn)
    } else {
      Some("player_1")
    }
  }

  // Add WebSocket connection
  def addConnection(connection: ActorRef): Unit = {
    webSocketConnections += connection
  }

  // Remove WebSocket connection
  def removeConnection(connection: ActorRef): Unit = {
    webSocketConnections -= connection
  }

  // Broadcast a message to all WebSocket connections
  def broadcast(message: String): Unit = {
    webSocketConnections.foreach { connection =>
      connection ! message
    }
  }
}