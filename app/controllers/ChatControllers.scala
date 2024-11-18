package controllers

import play.api.libs.json.Json
import play.api.mvc._
import javax.inject._
import scala.collection.mutable.ListBuffer

@Singleton
class ChatController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {
  private val messages = ListBuffer[String]()

  def getMessages: Action[AnyContent] = Action {
    Ok(Json.toJson(messages))
  }

  def sendMessage: Action[AnyContent] = Action { request =>
    val jsonBody = request.body.asJson
    jsonBody.flatMap(json => (json \ "message").asOpt[String]) match {
      case Some(message) =>
        messages += message
        Ok("Message received")
      case None =>
        BadRequest("Invalid message format")
    }
  }
}
