package actors

import actors.BookActor.BookDebited
import actors.BookView._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import helpers.DebugTimeout
import org.scalatest.{Matchers, WordSpecLike}
import support.EventLogContext

class BookViewSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with EventLogContext with DebugTimeout
  with WordSpecLike with Matchers {

  def this() = this(ActorSystem("BookViewSpec"))

  val owner = "TestUser"
  val aggregateId = "Book"

  "A book view" should {

    "increase balance and add book records when BookActor's `BookDebited` event is emitted" in {
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


      //    "be able to get book records when called with `GetBookRecords` command" in {
      //      val owner = "testUser"
      //      val bookName = UUID.randomUUID().toString
      //
      //      val bookView = system.actorOf(Props(BookView(owner, bookName, eventLog)))
      //
      //      bookView ! GetBookRecords
      //
      //      expectMsgPF(timeout) {
      //        case BookRecords(records) => records mustBe Seq()
      //        case _ => fail
      //      }
      //    }
    }
  }
}
