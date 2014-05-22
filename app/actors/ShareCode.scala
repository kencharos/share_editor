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
         // iteratee-入力.enumerator-出力。入力は何もしない
        val iteratee = Iteratee.ignore[JsValue] 
        (iteratee, reviewEnumerator)
    }
  }
}

class ShareCode extends Actor {
  // managerにのみ出力されればよいので、unicastでもいいのだが、上手い方法が思いつかないので、現状はbroadcast
  val (reviewEnumerator, reviewChanel) = Concurrent.broadcast[JsValue]
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
    reviewChanel.push(msg)
  }
}

case class Join(username: String)
case class Review()
case class Connected(enumerator: Enumerator[JsValue])

