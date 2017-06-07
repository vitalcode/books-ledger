package support

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import com.rbmhtechnology.eventuate.EventsourcingProtocol._
import com.rbmhtechnology.eventuate.{DurableEvent, VectorTime}
import org.scalatest.Matchers

trait EventLogContext extends Matchers {

  val logIdA = "logA"
  var instanceId: Int = _
  var logProbe: TestProbe = _
  var emitterId: String = _

  val instanceIdCounter = new AtomicInteger(0)

  def withEventLog(test: (ActorRef, String) => Unit)(implicit system: ActorSystem) = {
    instanceId = instanceIdCounter.getAndIncrement()
    emitterId = UUID.randomUUID().toString
    logProbe = TestProbe()
    test(logProbe.ref, emitterId)
  }

  def addEventToLog(actor: ActorRef, user: String, aggregateId: String, events: Any*) = {
    val userAggregateId = Some(s"$user-$aggregateId")

    logProbe.expectMsg(LoadSnapshot(emitterId, instanceId))
    logProbe.sender() ! LoadSnapshotSuccess(None, instanceId)

    logProbe.expectMsg(Replay(1, Some(actor), userAggregateId, instanceId))
    val durableEvents = events.map(DurableEvent(_, emitterId, Option(emitterId), Set(), 0L, VectorTime(logIdA -> 1L), logIdA, logIdA, instanceId)).toList
    logProbe.sender() ! ReplaySuccess(durableEvents, durableEvents.size, instanceId)

    logProbe.expectMsg(Replay(durableEvents.size + 1, None, userAggregateId, instanceId))
    logProbe.sender() ! ReplaySuccess(Nil, 0, instanceId)
  }

  def expectEvents(events: Any*)(implicit actor: ActorRef) = {

    val emitterIdA = "A"
    def event(payload: Any, sequenceNr: Long): DurableEvent = DurableEvent(payload, emitterIdA, None, Set(), 0L, VectorTime(logIdA -> sequenceNr), logIdA, logIdA, sequenceNr)

    val write = logProbe.expectMsgClass(classOf[Write])
    write.events.zipWithIndex.foreach{
      case (event, index) =>
        println (s"ass ${event.payload} -> ${events(index)}")
        event.payload shouldBe events(index)
    }

    val e = events.zipWithIndex.map {
      case (e, i) => event(e, i)
    }.toList

    logProbe.sender() ! WriteSuccess(e, write.correlationId, instanceId)
//    logProbe.sender() ! WriteSuccess(events.map(event(_, 1L)).toList, write.correlationId, instanceId)

  }
}