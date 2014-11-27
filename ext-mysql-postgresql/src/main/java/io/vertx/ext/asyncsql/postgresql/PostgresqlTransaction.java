package io.vertx.ext.asyncsql.postgresql;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.DatabaseCommands;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface PostgresqlTransaction extends DatabaseCommands {

  /**
   * Commits a transaction.
   *
   * @param resultHandler Callback if commit succeeded.
   */
  void commit(Handler<AsyncResult<String>> resultHandler);

  /**
   * Rolls back a transaction.
   *
   * @param resultHandler Callback if rollback succeeded.
   */
  void rollback(Handler<AsyncResult<Void>> resultHandler);

}
