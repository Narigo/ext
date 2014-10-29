package io.vertx.ext.rx.java.test;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.rx.java.ObservableHandler;
import io.vertx.ext.rx.java.RxHelper;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JavaIntegrationTest extends VertxTestBase {

  @Test
  public void testConsumeBodyStream() {
    EventBus eb = vertx.eventBus();
    MessageConsumer<String> consumer = eb.<String>consumer("the-address");
    Observable<String> obs = RxHelper.toObservable(consumer.bodyStream());
    List<String> items = new ArrayList<>();
    obs.subscribe(new Subscriber<String>() {
      @Override
      public void onNext(String s) {
        items.add(s);
        if (items.size() == 3) {
          unsubscribe();
        }
      }
      @Override
      public void onError(Throwable throwable) {
        System.out.println("throwable = " + throwable);
        fail(throwable.getMessage());
      }
      @Override
      public void onCompleted() {
        assertEquals(Arrays.asList("msg1", "msg2", "msg3"), items);
        assertFalse(consumer.isRegistered());
        testComplete();
      }
    });
    eb.send("the-address", "msg1");
    eb.send("the-address", "msg2");
    eb.send("the-address", "msg3");
    await();
  }

  @Test
  public void testRegisterAgain() {
    EventBus eb = vertx.eventBus();
    MessageConsumer<String> consumer = eb.<String>consumer("the-address");
    Observable<String> obs = RxHelper.toObservable(consumer.bodyStream());
    obs.subscribe(new Subscriber<String>() {
      @Override
      public void onNext(String s) {
        fail("Was not expecting item " + s);
      }
      @Override
      public void onError(Throwable throwable) {
        fail("Was not esxpecting error " + throwable.getMessage());
      }
      @Override
      public void onCompleted() {
        unsubscribe();
        obs.subscribe(new Subscriber<String>() {
          @Override
          public void onNext(String s) {
            assertEquals("msg1", s);
            unsubscribe();
          }
          @Override
          public void onError(Throwable throwable) {
            fail("Was not esxpecting error " + throwable.getMessage());
          }
          @Override
          public void onCompleted() {
            assertFalse(consumer.isRegistered());
            testComplete();
          }
        });
        eb.send("the-address", "msg1");
      }
    }).unsubscribe();
    await();
  }

  @Test
  public void testObservableUnsubscribeDuringObservation() {
    EventBus eb = vertx.eventBus();
    MessageConsumer<String> consumer = eb.<String>consumer("the-address");
    Observable<String> obs = RxHelper.toObservable(consumer.bodyStream());
    Observable<String> a = obs.take(4);
    List<String> obtained = new ArrayList<>();
    a.subscribe(new Subscriber<String>() {
      @Override
      public void onCompleted() {
        assertEquals(Arrays.asList("msg0", "msg1", "msg2", "msg3"), obtained);
        consumer.endHandler(v -> testComplete());
      }
      @Override
      public void onError(Throwable e) {
        fail(e.getMessage());
      }

      @Override
      public void onNext(String str) {
        obtained.add(str);
      }
    });
    for (int i = 0;i < 7;i++) {
      eb.send("the-address", "msg" + i);
    }
    await();
  }

  @Test
  public void testObservableNetSocket() {
    ObservableHandler<NetServer> onListen = RxHelper.observableHandler();
    onListen.subscribe(
        server -> vertx.createNetClient(new NetClientOptions()).connect(1234, "localhost", ar -> {
          assertTrue(ar.succeeded());
          NetSocket so = ar.result();
          so.write("foo");
          so.close();
        }),
        error -> fail(error.getMessage())
    );
    NetServer server = vertx.createNetServer(new NetServerOptions().setPort(1234).setHost("localhost"));
    Observable<NetSocket> socketObs = RxHelper.toObservable(server.connectStream());
    socketObs.subscribe(new Subscriber<NetSocket>() {
      @Override
      public void onNext(NetSocket o) {
        Observable<Buffer> dataObs = RxHelper.toObservable(o);
        dataObs.subscribe(new Observer<Buffer>() {

          LinkedList<Buffer> buffers = new LinkedList<>();

          @Override
          public void onNext(Buffer buffer) {
            buffers.add(buffer);
          }

          @Override
          public void onError(Throwable e) {
            fail(e.getMessage());
          }

          @Override
          public void onCompleted() {
            assertEquals(1, buffers.size());
            assertEquals("foo", buffers.get(0).toString("UTF-8"));
            server.close();
          }
        });
      }

      @Override
      public void onError(Throwable e) {
        fail(e.getMessage());
      }

      @Override
      public void onCompleted() {
        testComplete();
      }
    });
    server.listen(onListen.asHandler());
    await();
  }

  @Test
  public void testObservableWebSocket() {
    ObservableHandler<HttpServer> onListen = RxHelper.observableHandler();
    onListen.subscribe(
        server -> vertx.createHttpClient(new HttpClientOptions()).connectWebsocket(1234, "localhost", "/some/path", ws -> {
          ws.write(Buffer.buffer("foo"));
          ws.close();
        }),
        error -> fail(error.getMessage())
    );
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(1234).setHost("localhost"));
    Observable<ServerWebSocket> socketObs = RxHelper.toObservable(server.websocketStream());
    socketObs.subscribe(new Subscriber<ServerWebSocket>() {
      @Override
      public void onNext(ServerWebSocket o) {
        Observable<Buffer> dataObs = RxHelper.toObservable(o);
        dataObs.subscribe(new Observer<Buffer>() {

          LinkedList<Buffer> buffers = new LinkedList<>();

          @Override
          public void onNext(Buffer buffer) {
            buffers.add(buffer);
          }

          @Override
          public void onError(Throwable e) {
            fail(e.getMessage());
          }

          @Override
          public void onCompleted() {
            assertEquals(1, buffers.size());
            assertEquals("foo", buffers.get(0).toString("UTF-8"));
            server.close();
          }
        });
      }

      @Override
      public void onError(Throwable e) {
        fail(e.getMessage());
      }

      @Override
      public void onCompleted() {
        testComplete();
      }
    });
    server.listen(onListen.asHandler());
    await();
  }

  @Test
  public void testObservableHttpRequest() {
    ObservableHandler<HttpServer> onListen = RxHelper.observableHandler();
    onListen.subscribe(
        server -> {
          HttpClientRequest req = vertx.createHttpClient(new HttpClientOptions()).request(HttpMethod.PUT, 1234, "localhost", "/some/path", resp -> {
          });
          req.putHeader("Content-Length", "3");
          req.write("foo");
        },
        error -> fail(error.getMessage())
    );
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(1234).setHost("localhost"));
    Observable<HttpServerRequest> socketObs = RxHelper.toObservable(server.requestStream());
    socketObs.subscribe(new Subscriber<HttpServerRequest>() {
      @Override
      public void onNext(HttpServerRequest o) {
        Observable<Buffer> dataObs = RxHelper.toObservable(o);
        dataObs.subscribe(new Observer<Buffer>() {

          LinkedList<Buffer> buffers = new LinkedList<>();

          @Override
          public void onNext(Buffer buffer) {
            buffers.add(buffer);
          }

          @Override
          public void onError(Throwable e) {
            fail(e.getMessage());
          }

          @Override
          public void onCompleted() {
            assertEquals(1, buffers.size());
            assertEquals("foo", buffers.get(0).toString("UTF-8"));
            server.close();
          }
        });
      }

      @Override
      public void onError(Throwable e) {
        fail(e.getMessage());
      }

      @Override
      public void onCompleted() {
        testComplete();
      }
    });
    server.listen(onListen.asHandler());
    await();
  }
}
