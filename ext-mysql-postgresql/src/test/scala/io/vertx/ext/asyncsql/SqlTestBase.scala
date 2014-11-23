package io.vertx.ext.asyncsql

import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext
import io.vertx.test.core.VertxTestBase

import scala.concurrent.ExecutionContext

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait SqlTestBase extends VertxTestBase {
  protected val log: Logger
  implicit val executionContext: ExecutionContext = SimpleExecutionContext.apply(log)
}
