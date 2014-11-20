package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.{Configuration, QueryResult, RowData}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Vertx, Future => JFuture}
import io.vertx.ext.asyncsql.BaseSqlService
import io.vertx.ext.asyncsql.impl.pool.{AsyncConnectionPool, SimpleExecutionContext}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class BaseSqlServiceImpl(vertx: Vertx, config: JsonObject) extends BaseSqlService {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[BaseSqlServiceImpl])
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected val dbType: String

  private val configuration = getConfiguration(config, dbType)
  private val maxPoolSize = config.getInteger("maxPoolSize", 10)
  private val transactionTimeout = config.getInteger("transactionTimeout", 500)
  private val pool = AsyncConnectionPool(vertx, dbType, maxPoolSize, configuration)

  override def raw(command: String, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = pool.withConnection { c =>
    c.sendQuery(command).map(resultToJsonObject)
  }

  private def resultToJsonObject(qr: QueryResult): JsonObject = {
    val result = new JsonObject()
    result.put("message", qr.statusMessage)
    result.put("rows", qr.rowsAffected)

    qr.rows match {
      case Some(resultSet) =>
        val fields: List[String] = resultSet.columnNames.toList
        val zipped = resultSet.zipWithIndex
        val js = (new JsonObject() /: zipped) { case (json, (rowData, index)) =>
          json.put(index.toString, rowDataToString(rowData))
        }

        result.put("results", js.toString)
      case None =>
    }

    result
  }

  private def rowDataToString(rowData: RowData): String = "beep"

  private def dataToJson(data: Any): Any = data match {
    case null => null
    case x: Array[Byte] => x
    case x: Boolean => x
    case x: Number => x
    case x: String => x
    case x: JsonObject => x
    case x: JsonArray => x
    case x => x.toString
  }

  override def begin(resultHandler: Handler[AsyncResult[String]]): Unit = ???

  override def commit(transactionId: String, resultHandler: Handler[AsyncResult[String]]): Unit = ???

  override def rollback(transactionId: String, resultHandler: Handler[AsyncResult[Void]]): Unit = ???

  override def rawInTransaction(transactionId: String, command: String, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = ???

  override def stop(stopped: JFuture[Void]): Unit = {
    pool.close() onComplete {
      case Success(p) => stopped.complete()
      case Failure(ex) => stopped.fail(ex)
    }
  }

  override def start(started: JFuture[Void]): Unit = {
    started.complete()
  }

  private def getConfiguration(config: JsonObject, dbType: String) = {
    val host = config.getString("host", "localhost")
    val port = config.getInteger("port", defaultPort)
    val username = config.getString("username", defaultUser)
    val password = Option(config.getString("password")).orElse(defaultPassword)
    val database = Option(config.getString("database")).orElse(defaultDatabase)

    Configuration(username, host, port, password, database)
  }

  protected def defaultPort: Int

  protected def defaultDatabase: Option[String]

  protected def defaultUser: String

  protected def defaultPassword: Option[String]
}
