package io.vertx.ext.asyncsql.impl

import java.util.UUID

import com.github.mauricio.async.db.{Connection, Configuration, QueryResult, RowData}
import io.vertx.core.eventbus.Message
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Vertx, Future => JFuture}
import io.vertx.ext.asyncsql.{TransactionConnection, BaseSqlService}
import io.vertx.ext.asyncsql.impl.pool.{AsyncConnectionPool, SimpleExecutionContext}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class BaseSqlServiceImpl(vertx: Vertx, config: JsonObject) extends BaseSqlService with CommandImplementations {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[BaseSqlServiceImpl])
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected val dbType: String

  protected def defaultHost: String

  protected def defaultPort: Int

  protected def defaultDatabase: Option[String]

  protected def defaultUser: String

  protected def defaultPassword: Option[String]

  protected lazy val maxPoolSize: Integer = config.getInteger("maxPoolSize", 10)
  protected lazy val transactionTimeout: Integer = config.getInteger("transactionTimeout", 500)
  protected lazy val configuration: Configuration = getConfiguration(config, dbType)
  protected lazy val pool: AsyncConnectionPool = AsyncConnectionPool(vertx, dbType, maxPoolSize, configuration)

  protected def takeConnection(): Future[Connection] = pool.take()
  protected def freeConnection(conn: Connection): Future[_] = pool.giveBack(conn)

  private val registerAddress: String = UUID.randomUUID().toString

  override def begin(resultHandler: Handler[AsyncResult[TransactionConnection]]): Unit = {
    (for {
      conn <- pool.take()
      dbConn <- Future.successful(new TransactionConnection() with CommandImplementations {
        override protected def takeConnection(): Future[Connection] = Future.successful(conn)
        override protected def freeConnection(conn: Connection): Future[_] = Future.successful()

        override def rollback(resultHandler: Handler[AsyncResult[Void]]): Unit = {
          pool.giveBack(conn)
        }
        override def commit(resultHandler: Handler[AsyncResult[String]]): Unit = {
          pool.giveBack(conn)
        }
      })
    } yield resultHandler.handle(JFuture.completedFuture(dbConn))) recover {
      case ex: Throwable => resultHandler.handle(JFuture.completedFuture(ex))
    }
  }

  override def stop(stopped: Handler[AsyncResult[Void]]): Unit = {
    pool.close() onComplete {
      case Success(p) => stopped.handle(JFuture.completedFuture(null))
      case Failure(ex) => stopped.handle(JFuture.completedFuture(ex))
    }
  }

  override def start(started: Handler[AsyncResult[Void]]): Unit = {
    val mc = vertx.eventBus().localConsumer(registerAddress, new Handler[Message[JsonObject]] {
      override def handle(event: Message[JsonObject]): Unit = {
        val address = event.body().getString("register")
      }
    })
    started.handle(JFuture.completedFuture(null))
  }

  private def getConfiguration(config: JsonObject, dbType: String) = {
    val host = config.getString("host", defaultHost)
    val port = config.getInteger("port", defaultPort)
    val username = config.getString("username", defaultUser)
    val password = Option(config.getString("password")).orElse(defaultPassword)
    val database = Option(config.getString("database")).orElse(defaultDatabase)

    logger.info(s"host=$host, defaultHost=$defaultHost")
    Configuration(username, host, port, password, database)
  }

}
