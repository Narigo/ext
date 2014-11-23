package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.asyncsql.postgresql.PostgresqlService
import io.vertx.test.core.VertxTestBase
import org.junit.Test

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaTest extends SqlTestBase {
  override protected val log: Logger = LoggerFactory.getLogger(classOf[VertxTestBase])

  var postgresqlService: PostgresqlService = null
  val address = "campudus.postgresql"

  override def setUp(): Unit = {
    super.setUp()
    val config: JsonObject = new JsonObject().put("address", address)
    postgresqlService = PostgresqlService.create(vertx, config)
    val latch: CountDownLatch = new CountDownLatch(1)
    postgresqlService.start(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
  }

  override def tearDown(): Unit = {
    val latch: CountDownLatch = new CountDownLatch(1)
    postgresqlService.stop(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

  @Test
  def simpleSelectTest(): Unit = {
    postgresqlService.raw("SELECT 1 AS one", new Handler[AsyncResult[JsonObject]] {
      override def handle(event: AsyncResult[JsonObject]): Unit = {
        assertTrue(s"should succeed but got ${event.cause()}", event.succeeded())
        val res = event.result()
        assertNotNull(res)
        val expected = new JsonObject()
          .put("rows", 1)
          .put("results", new JsonArray().add(new JsonArray().add(1)))
          .put("fields", new JsonArray().add("one"))
        res.remove("message")
        assertEquals(expected, res)
        testComplete()
      }
    })
    await()
  }

  def arhToFuture[T](fn: Handler[AsyncResult[T]] => Unit): Future[T] = {
    val p = Promise[T]()
    fn(new Handler[AsyncResult[T]] {
      override def handle(event: AsyncResult[T]): Unit =
        if (event.succeeded()) p.success(event.result()) else p.failure(event.cause())
    })
    p.future
  }

  @Test
  def simpleTransactionTest(): Unit = {
    (for {
      connection <- arhToFuture(postgresqlService.begin)
      res <- arhToFuture((connection.raw _).curried("SELECT 1 AS one"))
    } yield {
      val res = new JsonObject()
      log.info(s"res=${res.encode()}")
      assertNotNull(res)
      val expected = new JsonObject()
        .put("rows", 1)
        .put("results", new JsonArray().add(new JsonArray().add(1)))
        .put("fields", new JsonArray().add("one"))
      res.remove("message")
      assertEquals(expected, res)
      testComplete()
    }) recover {
      case ex: Throwable => fail(s"should not get an excepction: $ex")
    }
    await()
  }

}
