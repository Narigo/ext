package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen(concrete = false)
public interface BaseSqlService extends DatabaseCommands {

  /**
   * Begins a transaction.
   *
   * @param resultHandler Callback to get the connection address back.
   */
  void begin(Handler<AsyncResult<TransactionConnection>> resultHandler);

  @ProxyIgnore
  void start(Handler<AsyncResult<Void>> whenDone);

  @ProxyIgnore
  void stop(Handler<AsyncResult<Void>> whenDone);

}
