package actors

import java.util.UUID

import actors.BookActor.{Credited, Debited}
import actors.BookView.{BookBalance, BookRecords, GetBookBalance, GetBookRecords}
import akka.actor.{ActorLogging, ActorRef}
import com.rbmhtechnology.eventuate.EventsourcedView

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
    id = "A",//UUID.randomUUID().toString,
    owner = owner,
    bookName = bookName,
    eventLog = eventLog
  )
}

case class BookView(id: String,
                    owner: String,
                    bookName: String,
                    eventLog: ActorRef) extends EventsourcedView with ActorLogging {

//  override val aggregateId: Option[String] = Some(s"$owner-$bookName")
  override val aggregateId: Option[String] = Some(bookName)

  private var balance: BigDecimal = 0
  private var records: Seq[BigDecimal] = Seq()

  override def onCommand: Receive = {
    case GetBookBalance =>
      sender() ! BookBalance(balance)
    case GetBookRecords => sender() ! BookRecords(records)
  }

  override def onEvent: Receive = {
    case Credited(creditAmount, _) =>
      appendRecord(creditAmount)
      balance = balance + creditAmount
    case Debited(debitAmount, _) =>
      appendRecord(debitAmount)
      balance = balance - debitAmount
  }

  private def appendRecord(record: BigDecimal) = {
    records :+ record
  }
}


