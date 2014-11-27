package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Future => VFuture}
import io.vertx.ext.asyncsql.DatabaseCommands
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait CommandImplementations extends DatabaseCommands {
  private val logger: Logger = LoggerFactory.getLogger(super.getClass)
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected def withConnection[T](fn: Connection => Future[T]): Future[T]

  override def raw(command: String, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = {
    logger.info(s"raw command -> $command")
    withConnection { connection =>
      (for {
        json <- connection.sendQuery(command).map(resultToJsonObject)
      } yield {
        resultHandler.handle(VFuture.succeededFuture(json))
      }) recover {
        case ex: Throwable =>
          logger.info(s"there was a problem with the connection: $ex")
          resultHandler.handle(VFuture.failedFuture(ex))
      }
    }
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
