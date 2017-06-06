package actors

import java.util.UUID

import actors.BookActor._
import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor

import scala.util.{Failure, Success}

object BookActor {

  //Commands
  case class BookCredit(amount: BigDecimal, note: String)

  case class BookDebit(amount: BigDecimal, note: String)

  //Replies
  case class BookOperationSuccess(amount: BigDecimal)

  case class BookOperationFailure(cause: Throwable)

  //Events
  trait Event

  case class BookCredited(amount: BigDecimal, note: String) extends Event

  case class BookDebited(amount: BigDecimal, note: String) extends Event

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
    case BookCredit(creditAmount, note) =>
      persistEvent(BookCredited(creditAmount, note))
    case BookDebit(debitAmount, note) => persistEvent(BookDebited(debitAmount, note))
  }

  override def onEvent: Receive = {
    case BookCredited(creditAmount, _) =>
      amount = amount + creditAmount

    case BookDebited(debitAmount, _) => amount = amount - debitAmount
  }

  private def persistEvent(event: Event) = {
    persist(event) {
      case Success(_) =>
        sender() ! BookOperationSuccess(amount)
      case Failure(cause) => sender() ! BookOperationFailure(cause)
    }
  }
}
