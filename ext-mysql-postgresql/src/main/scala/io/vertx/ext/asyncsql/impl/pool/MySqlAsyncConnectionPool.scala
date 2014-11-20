package io.vertx.ext.asyncsql.impl.pool

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.MySQLConnection
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory

import scala.concurrent.ExecutionContext

class MySqlAsyncConnectionPool(val vertx: Vertx, config: Configuration, eventLoop: EventLoop, val maxPoolSize: Int) extends AsyncConnectionPool {

  private val logger: Logger = LoggerFactory.getLogger(classOf[MySqlAsyncConnectionPool])

  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  override def create() = new MySQLConnection(
    configuration = config,
    group = eventLoop,
    executionContext = executionContext
  ).connect

}