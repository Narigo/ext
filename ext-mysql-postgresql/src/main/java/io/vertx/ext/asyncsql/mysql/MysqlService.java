package io.vertx.ext.asyncsql.mysql;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.BaseSqlService;
import io.vertx.ext.asyncsql.TransactionConnection;
import io.vertx.ext.asyncsql.mysql.impl.MysqlServiceImpl;
import io.vertx.proxygen.ProxyHelper;

import java.util.UUID;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen
@ProxyGen
public interface MysqlService extends BaseSqlService {

  static MysqlService create(Vertx vertx, JsonObject config) {
    return new MysqlServiceImpl(vertx, config);
  }

  static MysqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(MysqlService.class, vertx, address);
  }

}
