package actors

import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel

import models._

object ShareCode {

  implicit val timeout = Timeout(1 second)

  lazy val manager = {
    val actor = Akka.system.actorOf(Props[ShareCode])
    actor
  }

  // joinがContorlerからよばれ、WebSocketの戻り値である、Futer(Iteratee, Enumerator)を返す。
  def join(username: String): scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {

    // ?はAskパターン。actorにJoinを送り、結果(Future)をmapで戻り値を処理．
    // connectedで単一のenumeratorを取得し、Iterateeを作成する。
    (manager ? Join(username)).map {
      case Connected(reviewEnumerator) =>
        
        val iteratee = Iteratee.foreach[JsValue] {
          event => //ブラウザからのイベント取得,チャネルへコード内容を送信。
            //println("student event")
            manager ! Answer((event \ "theme").as[String], username, (event \ "content").as[String])
        }.mapDone {//終了時
          _ =>
            println("student done")
        }
        (iteratee, reviewEnumerator)
    }
  }

  def joinManager : scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    (manager ? Review()).map {
      case Connected(reviewEnumerator) =>
        
         // iteratee-入力.enumerator-出力。shutu
        val iteratee = Iteratee.foreach[JsValue] {
          event => //ブラウザからのイベント取得,チャネルへコード内容を送信。
            println("manager event")
            manager ! Answer((event \ "theme").as[String], (event \ "name").as[String], (event \ "content").as[String])
        }.mapDone {//終了時
          _ =>
            println("manager done")
        }
         println("manager connect")
        (iteratee, reviewEnumerator)
    }
  }
}

class ShareCode extends Actor {

  var reviewChanel: Option[Channel[JsValue]] = None
  def onStart: Channel[JsValue] => Unit = {
    channel =>
      reviewChanel = Some(channel)
      println("start:ShareCode:onstart")
  }

  def onError: (String, Input[JsValue]) => Unit = {
    (message, input) =>
      println("onError " + message)
  }

  def onComplete = println("onComplete")
  // 単一の送信先
  val reviewEnumerator = Concurrent.unicast[JsValue](onStart, onComplete, onError)

  def receive = {
    //開始点 出力先のenumartorを送信し、呼び出し元でiterateeとあわせてWebSocketを作成する。
    case Join(username) => 
      sender ! Connected(reviewEnumerator)
    case Answer(theme, name, content) => 
      sendAnswer(theme, name, content)
    case Review() =>
      sender ! Connected(reviewEnumerator)
  }

  def sendAnswer(theme: String, name: String, content: String) {
    val msg = JsObject(
      Seq(
        "theme" -> JsString(theme),
        "name" -> JsString(name),
        "content" -> JsString(content)
        )
    )
    reviewChanel match { // チャネルに書き出す。
      case Some(channel) => channel.push(msg)
      case _ => println("nothing")
    }
  }
}

case class Join(username: String)
case class Review()
case class Connected(enumerator: Enumerator[JsValue])

