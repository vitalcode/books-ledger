package support

import java.util.UUID

trait IdGenerator {
  def id = UUID.randomUUID().toString
}
