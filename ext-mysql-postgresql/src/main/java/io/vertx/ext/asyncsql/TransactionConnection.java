package io.vertx.ext.asyncsql;

import com.github.mauricio.async.db.Connection;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.proxygen.ProxyHelper;

import java.util.UUID;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
public interface TransactionConnection extends DatabaseCommands {

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
