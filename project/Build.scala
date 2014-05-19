import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "share_editor"
  val appVersion      = "1.1"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "websocket_plugin" % "websocket_plugin_2.10" % "0.4.0",
    "org.webjars" %% "webjars-play" % "2.2.1-2",
    "org.webjars" % "bootstrap" % "2.3.1",
    "org.webjars" % "ace" % "07.31.2013",
    "org.webjars" % "highlightjs" % "8.0-3"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(

      resolvers += Resolver.url("TPTeam Repository", url("http://tpteam.github.io/snapshots/"))(Resolver.ivyStylePatterns)

  )

}
