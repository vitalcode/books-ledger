package actors

import actors.TradeActor.{TradeCreated, TradeUpdated}
import actors.TradeAggregateViewActor.{UnWatchTrades, WatchTrades}
import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedView
import models.Trade

import scala.collection.immutable.HashSet
import scala.collection.mutable

object TradeAggregateViewActor {

  val ID = "464788cb-58aa-4dc6-8dce-703a456c238a"
  val NAME = "trade_view_aggregate"

  case object WatchTrades

  case object UnWatchTrades

}

class TradeAggregateViewActor(override val id: String,
  override val eventLog: ActorRef
) extends EventsourcedView {

  protected[this] var watchers = HashSet.empty[ActorRef]

  private val trades = mutable.Map[String, Trade]()

  override def onCommand: Receive = {

    case WatchTrades =>
      watchers = watchers + sender

    case UnWatchTrades =>
      watchers = watchers - sender

  }

  override def onEvent: Receive = {
    case TradeCreated(trade) =>
      trades(trade.id) = trade
      watchers.foreach(_ ! TradeCreated(trade))

    case TradeUpdated(trade) =>
      trades(trade.id) = trade
      watchers.foreach(_ ! TradeUpdated(trade))

  }
}
