package actors.helpers

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

trait DebugTimeout {
  val timeout = Duration(10, TimeUnit.MINUTES)
}

trait DefaultTimeout {
  val timeout = Duration(3, TimeUnit.SECONDS)
}
