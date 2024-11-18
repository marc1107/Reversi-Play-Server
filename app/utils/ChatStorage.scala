package utils

import scala.collection.mutable

object ChatStorage {
  private val messages = mutable.ListBuffer[String]()

  def addMessage(message: String): Unit = messages += message

  def getMessages: List[String] = messages.toList
}
