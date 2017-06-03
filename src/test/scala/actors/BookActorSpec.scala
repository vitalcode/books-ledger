package actors

import java.util.UUID
import java.util.concurrent.TimeUnit

import actors.BookActor.{BookOperationFailure, BookOperationSuccess, Credit}
import actors.helpers.DefaultTimeout
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.rbmhtechnology.eventuate.ReplicationEndpoint
import com.rbmhtechnology.eventuate.ReplicationEndpoint._
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.duration.Duration


class BookActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with DefaultTimeout
  with WordSpecLike with MustMatchers {

  def this() = this(ActorSystem("BookActorSpec"))

  val endpoint = ReplicationEndpoint(id => LeveldbEventLog.props(logId = "BookActorSpec"))(_system)
  val eventLog = endpoint.logs(DefaultLogName)

  "A book actor" must {

    "be able to credit book amount when called with `Credit` command" in {
      //Arrange
      val bookName = UUID.randomUUID().toString
      val bookActor = system.actorOf(Props(BookActor(bookName, eventLog, 10)))

      //Act
      bookActor ! Credit(2, "Some credit")

      //Assert
      expectMsgPF(timeout) {
        case BookOperationSuccess(amount) => amount mustBe 8
        case BookOperationFailure(_) => fail
      }
    }
  }
}
