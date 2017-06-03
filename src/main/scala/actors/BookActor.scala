package actors

import java.util.UUID

import actors.BookActor._
import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor

import scala.util.{Failure, Success}

object BookActor {

  //Commands
  case class Credit(amount: BigDecimal, note: String)

  case class Debit(amount: BigDecimal, note: String)

  //Replies
  case class BookOperationSuccess(amount: BigDecimal)

  case class BookOperationFailure(cause: Throwable)

  //Events
  trait Event

  case class Credited(amount: BigDecimal, note: String) extends Event

  case class Debited(amount: BigDecimal, note: String) extends Event

  def apply(bookName: String,
            eventLog: ActorRef,
            amount: BigDecimal) = new BookActor(
    id = UUID.randomUUID().toString,
    bookName = bookName,
    eventLog = eventLog,
    amount = amount
  )
}

case class BookActor(id: String,
                     bookName: String,
                     eventLog: ActorRef,
                     private var amount: BigDecimal) extends EventsourcedActor {

  override val aggregateId: Option[String] = Some(bookName)

  override def onCommand: Receive = {
    case Credit(creditAmount, note) =>
      persistEvent(Credited(creditAmount, note))
    case Debit(debitAmount, note) => persistEvent(Debited(debitAmount, note))
  }

  override def onEvent: Receive = {
    case Credited(creditAmount, _) =>
      amount = amount - creditAmount

    case Debited(debitAmount, _) => amount = amount + debitAmount
  }

  private def persistEvent(event: Event) = {
    persist(event) {
      case Success(_) =>
        sender() ! BookOperationSuccess(amount)
      case Failure(cause) => sender() ! BookOperationFailure(cause)
    }
  }
}
