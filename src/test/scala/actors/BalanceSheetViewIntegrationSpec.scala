package actors

import actors.BalanceSheetView.{BalanceSheetAssets, GetBalanceSheetAssets}
import actors.BookActor.{apply => _, unapply => _, _}
import actors.BookView.{BookBalance, GetBookBalance, _}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.rbmhtechnology.eventuate.ReplicationEndpoint
import com.rbmhtechnology.eventuate.ReplicationEndpoint._
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import helpers.DefaultTimeout
import org.scalatest.{FreeSpecLike, Matchers}
import support.IdGenerator


class BalanceSheetViewIntegrationSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with DefaultTimeout with IdGenerator
  with FreeSpecLike with Matchers {

  def this() = this(ActorSystem("BalanceSheetViewIntegrationSpec"))

  val endpoint = ReplicationEndpoint(id => LeveldbEventLog.props(logId = "BalanceSheetViewIntegrationSpec"))(_system)
  val eventLog = endpoint.logs(DefaultLogName)

  val owner = "TestUser"

  "BalanceSheetView should" - {

    "update assets amount accordingly if cash account is debited or credited" - {

      "increasing balance when BookActor receives `BookDebit` command" in {

        val aggregateId = id

        val balanceSheetView = system.actorOf(Props(new BalanceSheetView(id, owner, aggregateId, eventLog)))
        val cashBookActor = system.actorOf(Props(new BookActor(id, owner, aggregateId, eventLog)))

        cashBookActor ! BookDebit(2, "note")

        expectMsgPF(timeout) {
          case BookOperationSuccess(amount) => amount shouldBe 2
          case BookOperationFailure(_) => fail
        }

        balanceSheetView ! GetBalanceSheetAssets

        expectMsgPF(timeout) {
          case BalanceSheetAssets(amount) => amount shouldBe 2
          case _ => fail
        }
      }
    }
  }
}
