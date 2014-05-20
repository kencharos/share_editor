package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.data._
import play.api.data.Forms._

import models._

object Application extends Controller {

  val themeForm = Form(
    mapping(
      "theme" -> nonEmptyText,
      "name" -> nonEmptyText
    )(Challenger.apply)(Challenger.unapply)
  )

  val inputForm = Form(
    mapping(
      "theme" -> nonEmptyText,
      "content" -> nonEmptyText
    )(Theme.apply)(Theme.unapply)
  )

  def index = Action {implicit request =>
    Ok(views.html.index(Theme.all, themeForm))
  }

  def list = Action {implicit request =>
    Ok(views.html.themelist(Theme.all))
  }

  def newTheme = Action {implicit request =>
    Ok(views.html.themeinput(inputForm))
  }

  def create = Action {implicit request =>
    inputForm.bindFromRequest.fold(
      // on validation error
      errors => BadRequest(views.html.themeinput(inputForm)),
      // validation OK.
      input => {
        Theme.save(input)
        Redirect(routes.Application.list())
      }
    )
  }

  def delete(theme:String) = Action { implicit request =>
    Theme.delete(theme)
    Redirect(routes.Application.list())
  }

  
  def share =  Action {implicit request =>
    themeForm.bindFromRequest.fold(
      // on validation error
      errors => BadRequest(views.html.index(Theme.all, errors)),
      // validation OK.
      challenger => {
        val theme = Theme.get(challenger.theme).get
       
        Ok(views.html.editor(Answer(theme.theme, challenger.name, theme.content)))
      }
    )
  }

  def review = TODO

  def shareWs = 
    WebSocket.async[JsValue] {implicit request => 
    actors.ws.ExampleWs.control
  }
  /*
  def indexStateless = Action {implicit request =>
    Ok(views.html.indexStateless("Welcome! Let's Play with your brand new stateless websocket!")).withSession(
        ("uuid" -> java.util.UUID.randomUUID.toString)
      )
  }
  def indexStatefull = Action {implicit request =>
    Ok(views.html.indexStatefull("Welcome! Let's Play with your brand new statefull websocket!")).withSession(
        ("uuid" -> java.util.UUID.randomUUID.toString)
      )
  }
  */
  
}
