package actors

import actors.BookActor.{Credited, Debited}
import actors.BookView._
import actors.helpers.DebugTimeout
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.rbmhtechnology.eventuate.EventsourcingProtocol.{LoadSnapshot, LoadSnapshotSuccess, Replay, ReplaySuccess}
import com.rbmhtechnology.eventuate.{DurableEvent, VectorTime}
import org.scalatest.{MustMatchers, WordSpecLike}

class BookViewSpec(_system: ActorSystem) extends TestKit(_system) with DebugTimeout with ImplicitSender
  with WordSpecLike with MustMatchers {

  def this() = this(ActorSystem("BookViewSpec5"))

  "A book view" must {


    "be increase balance by listening on BookActor's `Credited` event" in new Context {
      withLog { log =>

        val bookView = system.actorOf(Props(BookView(owner, emitterIdA, log)))

        addEventToLog(bookView,
          Credited(1, "note"),
          Credited(4, "note"),
          Debited(2, "note")
        )

        bookView ! GetBookBalance

        expectMsgPF(timeout) {
          case BookBalance(amount) => amount mustBe 3
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

    trait Context {

      val owner = "testUser"
      val emitterIdA = "A"

      val logIdA = "logA"
      val logIdB = "logB"

      val instanceId = 0

      val logProbe = TestProbe()

      def timestamp(a: Long = 0L, b: Long = 0L) = (a, b) match {
        case (0L, 0L) => VectorTime()
        case (a, 0L) => VectorTime(logIdA -> a)
        case (0L, b) => VectorTime(logIdB -> b)
        case (a, b) => VectorTime(logIdA -> a, logIdB -> b)
      }

      def withLog(test: ActorRef => Unit) = {
        test(logProbe.ref)
      }

      def addEventToLog(actor: ActorRef, events: Any*) = {
        logProbe.expectMsg(LoadSnapshot(emitterIdA, 0))
        logProbe.sender() ! LoadSnapshotSuccess(None, instanceId)

        logProbe.expectMsg(Replay(1, Some(actor), Some(emitterIdA), 0))
        val durableEvents = events.map(DurableEvent(_, emitterIdA, Option(emitterIdA), Set(), 0L, timestamp(1, 0), logIdA, logIdA, instanceId)).toList
        logProbe.sender() ! ReplaySuccess(durableEvents, durableEvents.size, instanceId)

        logProbe.expectMsg(Replay(durableEvents.size + 1, None, Some(emitterIdA), instanceId))
        logProbe.sender() ! ReplaySuccess(Nil, 0, instanceId)
      }
    }
  }
}
