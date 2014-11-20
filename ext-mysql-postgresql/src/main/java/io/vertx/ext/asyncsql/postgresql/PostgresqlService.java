package io.vertx.ext.asyncsql.postgresql;

import io.vertx.ext.asyncsql.postgresql.impl.PostgresqlServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.BaseSqlService;
import io.vertx.proxygen.ProxyHelper;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen(concrete = false)
@ProxyGen
public interface PostgresqlService extends BaseSqlService {

    static PostgresqlService create(Vertx vertx, JsonObject config) {
        return new PostgresqlServiceImpl(vertx, config);
    }

    static PostgresqlService createEventBusProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(PostgresqlService.class, vertx, address);
    }

}
