import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

object FrontendRunHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {
      var process: Option[Process] = None
      var install: String = FrontendCommands.dependencyInstall
      var run: String = FrontendCommands.serve

      override def beforeStarted(): Unit = {
        if (!(base / "ui" / "node_modules").exists()) Process(install, base / "ui").!
      }

      override def afterStarted(): Unit = {
        process = Some(
          Process(run, base / "ui", "CI" -> "true").run
        )
      }

      override def afterStopped(): Unit = {
        process.foreach(p => p.destroy())
        process = None
      }
    }
    UIBuildHook
  }
}
