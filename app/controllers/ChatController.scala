package controllers

import play.api.libs.json.Json
import play.api.mvc.*
import utils.ChatStorage

import javax.inject.*
import scala.collection.mutable.ListBuffer

@Singleton
class ChatController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {
  def getMessages: Action[AnyContent] = Action {
    Ok(Json.toJson(ChatStorage.messages))
  }

  def sendMessage: Action[AnyContent] = Action { request =>
    val jsonBody = request.body.asJson
    jsonBody.flatMap(json => (json \ "message").asOpt[String]) match {
      case Some(message) =>
        ChatStorage.messages += message
        Ok("Message received")
      case None =>
        BadRequest("Invalid message format")
    }
  }
}
