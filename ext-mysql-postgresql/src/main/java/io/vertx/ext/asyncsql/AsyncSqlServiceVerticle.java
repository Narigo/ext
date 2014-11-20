package io.vertx.ext.asyncsql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.asyncsql.mysql.MysqlService;
import io.vertx.ext.asyncsql.postgresql.PostgresqlService;
import io.vertx.proxygen.ProxyHelper;

/**
 * @author <a href="http://www.campudus.com/">Joern Bernhardt</a>
 */
public class AsyncSqlServiceVerticle extends AbstractVerticle {

  PostgresqlService postgresqlService;
  MysqlService mysqlService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    final String dbType = config().getString("dbType");

    String address = config().getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }

    // Create the service object
    if ("postgresql".equals(dbType)) {
      postgresqlService = PostgresqlService.create(vertx, config());
      ProxyHelper.registerService(PostgresqlService.class, vertx, postgresqlService, address);

      // Start it
      postgresqlService.start(res -> {
        if (res.failed()) {
          startFuture.fail(res.cause());
        } else {
          startFuture.complete();
        }
      });
    } else if ("mysql".equals(dbType)) {
      mysqlService = MysqlService.create(vertx, config());
      ProxyHelper.registerService(MysqlService.class, vertx, mysqlService, address);

      // Start it
      mysqlService.start(res -> {
        if (res.failed()) {
          startFuture.fail(res.cause());
        } else {
          startFuture.complete();
        }
      });
    }

  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    if (postgresqlService != null) {
      postgresqlService.stop(res -> {
        if (res.failed()) {
          stopFuture.fail(res.cause());
        } else {
          stopFuture.complete();
        }
      });
    } else if (mysqlService != null) {
      mysqlService.stop(res -> {
        if (res.failed()) {
          stopFuture.fail(res.cause());
        } else {
          stopFuture.complete();
        }
      });
    }
  }
}