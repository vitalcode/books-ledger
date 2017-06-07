package actors

import actors.BookActor._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import helpers.DebugTimeout
import org.scalatest.{FreeSpecLike, Matchers}
import support.EventLogContext

class BookActorSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with EventLogContext with DebugTimeout
  with FreeSpecLike with Matchers {

  def this() = this(ActorSystem("BookActorSpec"))

  val owner = "TestUser"
  val aggregateId = "Book"

  "BookActor should" - {
    "be able to increase book balance when called with `BookDebit` command" in {
      withEventLog { (eventLog, emitterId) =>

        val bookActor = system.actorOf(Props(new BookActor(emitterId, owner, aggregateId, eventLog)))

        addEventToLog(bookActor, owner, aggregateId,
          BookDebited(6, "", "note"),
          BookDebited(6, "", "note")
        )

        bookActor ! BookDebit(3, "note")
        bookActor ! BookDebit(4, "note")

        expectEvents(BookDebited(3, "", "note"), BookDebited(4, "", "note"))

        expectMsg(BookOperationSuccess(15))
        expectMsg(BookOperationSuccess(19))
      }
    }
    "be able to decrease book balance when called with `BookCredit` command" in {
      withEventLog { (eventLog, emitterId) =>
        val bookActor = system.actorOf(Props(new BookActor(emitterId, owner, aggregateId, eventLog)))

        addEventToLog(bookActor, owner, aggregateId,
          BookDebited(6, "", "note"),
          BookDebited(6, "", "note")
        )

        bookActor ! BookCredit(3, "note")
        bookActor ! BookCredit(1, "note")

        expectEvents(BookCredited(3, "", "note"))
        expectEvents(BookCredited(1, "", "note"))

        expectMsg(BookOperationSuccess(9))
        expectMsg(BookOperationSuccess(8))
      }
    }
  }
}
