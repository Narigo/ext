package io.vertx.ext.asyncsql.mysql.impl

import io.vertx.core.{Future, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.BaseSqlServiceImpl
import io.vertx.ext.asyncsql.mysql.MysqlService

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlServiceImpl(vertx: Vertx, config: JsonObject)
  extends BaseSqlServiceImpl(vertx, config) with MysqlService {

  override protected val dbType: String = "mysql"

  override protected val defaultPort: Int = 3306

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "root"

  override protected val defaultPassword: Option[String] = None

}
