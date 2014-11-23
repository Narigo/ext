package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.{Connection, Configuration, QueryResult, RowData}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Future => JFuture}
import io.vertx.ext.asyncsql.DatabaseCommands
import io.vertx.ext.asyncsql.impl.pool.{SimpleExecutionContext, AsyncConnectionPool}

import scala.concurrent.{Future, ExecutionContext}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait CommandImplementations extends DatabaseCommands {
  private val logger: Logger = LoggerFactory.getLogger(super.getClass)

  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected def takeConnection(): Future[Connection]
  protected def freeConnection(conn: Connection): Future[_]

  override def raw(command: String, resultHandler: Handler[AsyncResult[JsonObject]]): Unit =
    (for {
      conn <- takeConnection()
      json <- conn.sendQuery(command).map(resultToJsonObject)
    } yield {
      freeConnection(conn)
      resultHandler.handle(JFuture.completedFuture(json))
    }) recover {
      case ex: Throwable =>
        logger.info(s"there was a problem with the connection: $ex")
        resultHandler.handle(JFuture.completedFuture(ex))
    }

  private def resultToJsonObject(qr: QueryResult): JsonObject = {
    val result = new JsonObject()
    result.put("message", qr.statusMessage)
    result.put("rows", qr.rowsAffected)

    qr.rows match {
      case Some(resultSet) =>
        val fields = (new JsonArray() /: resultSet.columnNames) {
          (arr, name) =>
            arr.add(name)
        }

        val rows = (new JsonArray() /: resultSet) {
          (arr, rowData) =>
            arr.add(rowDataToJsonArray(rowData))
        }

        result.put("fields", fields)
        result.put("results", rows)
      case None =>
    }

    result
  }

  private def rowDataToJsonArray(rowData: RowData): JsonArray = {
    val arr = new JsonArray()
    for {
      elem <- rowData.map(dataToJson).toList
    } {
      arr.add(elem)
    }
    arr
  }

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

}
