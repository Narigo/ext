package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen(concrete = false)
public interface BaseSqlService {

    /**
     * Begins a transaction.
     *
     * @param resultHandler Callback to use the transaction id.
     */
    void begin(Handler<AsyncResult<String>> resultHandler);

    /**
     * Commits a transaction.
     *
     * @param transactionId The UUID of the transaction.
     * @param resultHandler Callback if commit succeeded.
     */
    void commit(String transactionId, Handler<AsyncResult<String>> resultHandler);

    /**
     * Rolls back a transaction.
     *
     * @param transactionId The UUID of the transaction.
     * @param resultHandler Callback if rollback succeeded.
     */
    void rollback(String transactionId, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Sends a raw command to the database.
     *
     * @param command       The command to send.
     * @param resultHandler Callback to handle the result.
     */
    void raw(String command, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Sends a raw command to the database inside a transaction. You need to create transaction first, to be able to use
     * this method by using beginTransaction.
     *
     * @param transactionId The UUID of the transaction.
     * @param command       The command to send.
     * @param resultHandler Callback to handle the result.
     */
    void rawInTransaction(String transactionId, String command, Handler<AsyncResult<JsonObject>> resultHandler);

    @ProxyIgnore
    void start(Handler<AsyncResult<Void>> whenDone);

    @ProxyIgnore
    void stop(Handler<AsyncResult<Void>> whenDone);

}
