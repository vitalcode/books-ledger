package actors

import java.util.UUID
import java.util.concurrent.TimeUnit

import actors.BookActor.{BookOperationFailure, BookOperationSuccess, BookCredit, BookDebit}
import actors.BookView.{BookBalance, GetBookBalance}
import helpers.{DebugTimeout, DefaultTimeout}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.rbmhtechnology.eventuate.ReplicationEndpoint
import com.rbmhtechnology.eventuate.ReplicationEndpoint._
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.duration.Duration


class BookActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with DebugTimeout
  with WordSpecLike with MustMatchers {

  def this() = this(ActorSystem("BookActorSpec"))

  val endpoint = ReplicationEndpoint(id => LeveldbEventLog.props(logId = "BookActorSpec"))(_system)
  val eventLog = endpoint.logs(DefaultLogName)

  "A book actor" must {

    "be able to credit book amount when called with `Credit` command" in {
      val bookName = UUID.randomUUID().toString
      val bookActor = system.actorOf(Props(BookActor(bookName, eventLog, 10)))

      bookActor ! BookCredit(2, "Some credit")

      expectMsgPF(timeout) {
        case BookOperationSuccess(amount) => amount mustBe 12
        case BookOperationFailure(_) => fail
      }
    }

    "be able to debit book amount when called with `Debit` command" in {
      val bookName = UUID.randomUUID().toString
      val bookActor = system.actorOf(Props(BookActor(bookName, eventLog, 10)))

      bookActor ! BookDebit(2, "Some debit")

      expectMsgPF(timeout) {
        case BookOperationSuccess(amount) => amount mustBe 8
        case BookOperationFailure(_) => fail
      }
    }

//    "be able to debit book amount from view when called with `Debit` command" in {
//      val bookName = UUID.randomUUID().toString
//      val owner = "testUser"
//      val bookActor = system.actorOf(Props(BookActor(bookName, eventLog, 10)))
//      val bookView = system.actorOf(Props(BookView(owner, bookName, eventLog)))
//
//      bookActor ! Debit(2, "Some debit")
//
//      expectMsgPF(timeout) {
//        case BookOperationSuccess(amount) => amount mustBe 8
//        case BookOperationFailure(_) => fail
//      }
//
//      bookView ! GetBookBalance
//
//      expectMsgPF(timeout) {
//        case BookBalance(amount) => amount mustBe -2
//        case _ => fail
//      }
//    }
  }
}
