package actors

import actors.BookActor.{BookCredited, BookDebited}
import actors.BookView._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import helpers.DebugTimeout
import org.scalatest.{FreeSpecLike, Matchers}
import support.EventLogContext

class BookViewSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with EventLogContext with DebugTimeout
  with FreeSpecLike with Matchers {

  def this() = this(ActorSystem("BookViewSpec"))

  val owner = "TestUser"
  val aggregateId = "Book"

  "BookView should" - {
    "add book record and" - {
      "increase balance when BookActor's `BookDebited` event is emitted" in {
        withEventLog { (eventLog, emitterId) =>

          val bookView = system.actorOf(Props(new BookView(emitterId, owner, aggregateId, eventLog)))

          addEventToLog(bookView, owner, aggregateId,
            BookDebited(6, "note"),
            BookDebited(6, "note")
          )

          bookView ! GetBookBalance

          expectMsgPF(timeout) {
            case BookBalance(amount) => amount shouldBe 12
            case _ => fail
          }

          bookView ! GetBookRecords

          expectMsgPF(timeout) {
            case BookRecords(records) => records shouldBe Seq(6, 6)
            case _ => fail
          }
        }
      }
      "decrease balance when BookActor's `BookCredited` event is emitted" in {
        withEventLog { (eventLog, emitterId) =>

          val bookView = system.actorOf(Props(new BookView(emitterId, owner, aggregateId, eventLog)))

          addEventToLog(bookView, owner, aggregateId,
            BookDebited(6, "note"),
            BookDebited(6, "note"),
            BookCredited(3, "note")
          )

          bookView ! GetBookBalance

          expectMsgPF(timeout) {
            case BookBalance(amount) => amount shouldBe 9
            case _ => fail
          }

          bookView ! GetBookRecords

          expectMsgPF(timeout) {
            case BookRecords(records) => records shouldBe Seq(6, 6, 3)
            case _ => fail
          }
        }
      }
    }
  }
}
