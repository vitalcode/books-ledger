package actors

import java.util.UUID

import actors.BookActor.{BookCredited, BookDebited}
import actors.BookView.{BookBalance, BookRecords, GetBookBalance, GetBookRecords}
import akka.actor.{ActorLogging, ActorRef}
import com.rbmhtechnology.eventuate.EventsourcedView

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object BookView {

  //Command
  case object GetBookBalance

  case object GetBookRecords

  //Replies
  case class BookBalance(amount: BigDecimal)

  case class BookRecords(records: Seq[BigDecimal])

  def apply(owner: String,
            bookName: String,
            eventLog: ActorRef): BookView  = BookView(
    id = UUID.randomUUID().toString,
    owner = owner,
    bookName = bookName,
    eventLog = eventLog
  )
}

case class BookView(id: String,
                    owner: String,
                    bookName: String,
                    eventLog: ActorRef) extends EventsourcedView with ActorLogging {

  override val aggregateId: Option[String] = Some(s"$owner-$bookName")

  private var balance: BigDecimal = 0
  private val records: ArrayBuffer[BigDecimal] = ArrayBuffer()

  override def onCommand: Receive = {
    case GetBookBalance => sender() ! BookBalance(balance)
    case GetBookRecords => sender() ! BookRecords(records)
  }

  override def onEvent: Receive = {
    case BookDebited(debitAmount, _) =>
      appendRecord(debitAmount)
      balance = balance + debitAmount
    case BookCredited(creditAmount, _) =>
      appendRecord(creditAmount)
      balance = balance - creditAmount
  }

  private def appendRecord(record: BigDecimal) = {
    records += record
  }
}


