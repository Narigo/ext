package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen(concrete = false)
public interface DatabaseCommands {

  /**
   * Sends a raw command to the database.
   *
   * @param command       The command to send.
   * @param resultHandler Callback to handle the result.
   */
  void raw(String command, Handler<AsyncResult<JsonObject>> resultHandler);

}
