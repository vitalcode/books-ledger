package actors

import actors.BookActor.{apply => _, unapply => _, _}
import actors.BookView.{BookBalance, GetBookBalance, _}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.rbmhtechnology.eventuate.ReplicationEndpoint
import com.rbmhtechnology.eventuate.ReplicationEndpoint._
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import helpers.DefaultTimeout
import org.scalatest.{FreeSpecLike, Matchers}
import support.IdGenerator


class BookViewIntegrationSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with DefaultTimeout with IdGenerator
  with FreeSpecLike with Matchers {

  def this() = this(ActorSystem("BookViewIntegrationSpec"))

  val endpoint = ReplicationEndpoint(id => LeveldbEventLog.props(logId = "BookViewIntegrationSpec"))(_system)
  val eventLog = endpoint.logs(DefaultLogName)

  val owner = "TestUser"

  "BookView should collaborate with BookActor by" - {

    "recording records and" - {

      "increasing balance when BookActor receives `BookDebit` command" in {

        val aggregateId = id

        val bookView = system.actorOf(Props(new BookView(id, owner, aggregateId, eventLog)))
        val bookActor = system.actorOf(Props(new BookActor(id, owner, aggregateId, eventLog)))


        bookActor ! BookDebit(2, "note")

        expectMsgPF(timeout) {
          case BookOperationSuccess(amount) => amount shouldBe 2
          case BookOperationFailure(_) => fail
        }

        bookView ! GetBookBalance

        expectMsgPF(timeout) {
          case BookBalance(amount) => amount shouldBe 2
          case _ => fail
        }

        bookView ! GetBookRecords

        expectMsgPF(timeout) {
          case BookRecords(records) => records shouldBe Seq(2)
          case _ => fail
        }
      }

      "decreasing balance when BookActor receives `BookCredit` command" in {

        val aggregateId = id

        val bookView = system.actorOf(Props(new BookView(id, owner, aggregateId, eventLog)))
        val bookActor = system.actorOf(Props(new BookActor(id, owner, aggregateId, eventLog)))


        bookActor ! BookCredit(2, "note")

        expectMsgPF(timeout) {
          case BookOperationSuccess(amount) => amount shouldBe -2
          case BookOperationFailure(_) => fail
        }

        bookView ! GetBookBalance

        expectMsgPF(timeout) {
          case BookBalance(amount) => amount shouldBe -2
          case _ => fail
        }

        bookView ! GetBookRecords

        expectMsgPF(timeout) {
          case BookRecords(records) => records shouldBe Seq(2)
          case _ => fail
        }
      }
    }
  }
}
