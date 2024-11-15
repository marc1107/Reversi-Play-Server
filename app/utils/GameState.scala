package utils

import org.apache.pekko.actor.ActorRef

object GameState {
  var connectedPlayers: Set[String] = Set()
  var webSocketConnections: Set[ActorRef] = Set()

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