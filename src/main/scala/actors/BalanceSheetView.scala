package actors

import actors.BalanceSheetView._
import actors.BookActor.{BookCredited, BookDebited}
import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedView

object BalanceSheetView {

  //Command
  case object GetBalanceSheetAssets

  case object GetBalanceSheetEquity

  //Replies
  case class BalanceSheetAssets(amount: BigDecimal)

  case class BalanceSheetEquity(amount: BigDecimal)

}

case class BalanceSheetView(id: String,
                            owner: String,
                            bookName: String,
                            eventLog: ActorRef) extends EventsourcedView {

  override val aggregateId: Option[String] = Some(s"$owner-$bookName")

  private var assets: BigDecimal = 0
  private var equity: BigDecimal = 0

  override def onCommand: Receive = {
    case GetBalanceSheetAssets => sender() ! BalanceSheetAssets(assets)
    case GetBalanceSheetEquity => sender() ! BalanceSheetEquity(equity)
  }

  override def onEvent: Receive = {
    case BookDebited(amount, "cash", _) => assets += amount

    case BookCredited(amount, "cash", _) => assets -= amount

    case BookDebited(amount, "stock", _) => equity += amount

    case BookCredited(amount, "stock", _) => equity -= amount

    case BookDebited(amount, _, _) => assets += amount

    case BookCredited(amount, _, _) => assets -= amount
  }
}


