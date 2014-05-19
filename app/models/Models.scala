package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Theme(theme:String, content:String)
case class Challenger(theme:String, name:String)
case class Answer(theme:String, name:String, content:String = "")

object Theme {

  val parse = {
    str("theme") ~ str("content") map{
      case t~c => Theme(t,c)}
  }

  def all = DB.withConnection { implicit c => 
    SQL("select * from Theme").as(parse * )
  }

  def save(theme:Theme) = DB.withConnection { implicit c => 
    SQL("insert into Theme values({t}, {c})").on('t ->theme.theme, 'c -> theme.content).executeUpdate();
  }
  def delete(theme:Theme) = DB.withConnection { implicit c => 
    SQL("delete from Theme where theme = {theme}").on('theme ->theme.theme).executeUpdate();
  }
  def get(theme:String) = DB.withConnection { implicit c => 
    val list = SQL("select * from Theme where theme = {theme}").on('theme ->theme).as(parse * )
    list match {
      case Nil => None
      case _ => Some(list.head)
    }
  }
}
