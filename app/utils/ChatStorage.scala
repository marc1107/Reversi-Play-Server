package utils

import scala.collection.mutable.ListBuffer

object ChatStorage {
  val messages = ListBuffer[String]()

  def addMessage(message: String): Unit = messages += message

  def getMessages: List[String] = messages.toList
}
