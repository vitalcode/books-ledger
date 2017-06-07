package actors

import java.util.UUID

import actors.BookActor._
import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.{EventsourcedActor, EventsourcedView}

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
}

case class BookActor(id: String,
                     owner: String,
                     bookName: String,
                     eventLog: ActorRef) extends EventsourcedActor {

  override val aggregateId: Option[String] = Some(s"$owner-$bookName")

  private var balance: BigDecimal = 0

  override def onCommand: Receive = {
    case BookCredit(creditAmount, note) =>
      persistEvent(BookCredited(creditAmount, note))
    case BookDebit(debitAmount, note) =>
      //sender() ! BookOperationSuccess(balance)
      persistEvent(BookDebited(debitAmount, note))
  }

  override def onEvent: Receive = {
    case BookCredited(creditAmount, _) =>
      balance = balance - creditAmount
    case BookDebited(debitAmount, _) =>
      println(debitAmount)
      balance = balance + debitAmount
  }

  private def persistEvent(event: Event) = {
    persist(event) {
      case Success(_) =>
        sender() ! BookOperationSuccess(balance)
      case Failure(cause) =>
        sender() ! BookOperationFailure(cause)
    }
  }
}
