import io.vertx.groovy.core.Context
import io.vertx.groovy.core.Vertx
import rx.Observable
import rx.Observer

import java.util.concurrent.TimeUnit

Vertx vertx = Vertx.vertx();
vertx.runOnContext({
  long startTime = System.currentTimeMillis();
  Context initCtx = vertx.context();
  Observable.timer(100, 100, TimeUnit.MILLISECONDS, vertx.scheduler()).take(10).subscribe(new Observer<Long>() {
    public void onNext(Long value) {
      test.assertEquals(initCtx.delegate, vertx.context().delegate);
    }
    public void onError(Throwable e) {
      test.fail("unexpected failure");
    }
    public void onCompleted() {
      long timeTaken = System.currentTimeMillis() - startTime;
      test.assertTrue(Math.abs(timeTaken - 1000) < 100);
      test.testComplete();
    }
  });
});
test.await();
