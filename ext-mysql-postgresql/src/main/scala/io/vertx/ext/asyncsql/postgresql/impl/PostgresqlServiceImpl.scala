package io.vertx.ext.asyncsql.postgresql.impl

import com.github.mauricio.async.db.Configuration
import io.vertx.core.eventbus.Message
import io.vertx.core.{Handler, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.BaseSqlServiceImpl
import io.vertx.ext.asyncsql.postgresql.PostgresqlService

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlServiceImpl(vertx: Vertx, config: JsonObject)
  extends BaseSqlServiceImpl(vertx, config) with PostgresqlService {


  override protected val dbType: String = "postgresql"

  override protected val defaultPort: Int = 5432

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "vertx"

  override protected val defaultPassword: Option[String] = Some("test")

}
