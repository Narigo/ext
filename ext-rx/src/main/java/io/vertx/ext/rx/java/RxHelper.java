package io.vertx.ext.rx.java;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * A set of helpers for RxJava {@link Observable} with Vert.x {@link ReadStream} and
 * {@link io.vertx.core.AsyncResult} handlers.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RxHelper {


  /**
   * Adapts a Vert.x {@link io.vertx.core.streams.ReadStream<T>} to an RxJava {@link Observable<T>}. After
   * the stream is adapted to an observable, the original stream handlers should not be used anymore
   * as they will be used by the observable adapter.<p>
   *
   * @param stream the stream to adapt
   * @return the adapted observable
   */
  public static <T> Observable<T> toObservable(ReadStream<T> stream) {
    return Observable.create(new HandlerAdapter<>(stream));
  }

  /**
   * Create a new {@code ObservableHandler<T>} object: an {@link rx.Observable} implementation
   * implementing {@code Handler<AsyncResult<T>>}. When the async result handler completes, the observable
   * will produce the result and complete immediatly after, when it fails it will signal the error.
   *
   * @return the observable future.
   */
  public static <T> ObservableHandler<T> observableHandler() {
    return new ObservableHandler<T>();
  }

  /**
   * Adapt a {@link Subscriber} as a {@code Handler<AsyncResult<T>>;}.
   *
   * @param subscriber the subscriber to adapt
   * @return a {@code Handler<AsyncResult<T>>}
   */
  public static <T> Handler<AsyncResult<T>> toHandler(Observer<T> subscriber) {
    ObservableHandler<T> observable = RxHelper.<T>observableHandler();
    observable.subscribe(subscriber);
    return observable.asHandler();
  }

  /**
   * Adapt an item callback as a {@code Handler<AsyncResult<T>>}.
   *
   * @param onNext the {@code Action1<T>} you have designed to accept the resolution from the {@code Handler<AsyncResult<T>>}
   * @return a {@code Handler<AsyncResult<T>>}
   */
  public static <T> Handler<AsyncResult<T>> toHandler(Action1<T> onNext) {
    ObservableHandler<T> observable = RxHelper.<T>observableHandler();
    observable.subscribe(onNext);
    return observable.asHandler();
  }

  /**
   * Adapt an item callback and an error callback as a {@code Handler<AsyncResult<T>>}.
   *
   * @param onNext the {@code Action1<T>} you have designed to accept the resolution from the {@code Handler<AsyncResult<T>>}
   * @param onError the {@code Action1<Throwable>} you have designed to accept the eventual failure from the {@code Handler<AsyncResult<T>>}
   * @return a {@code Handler<AsyncResult<T>>}
   */
  public static <T> Handler<AsyncResult<T>> toHandler(Action1<T> onNext, Action1<Throwable> onError) {
    ObservableHandler<T> observable = RxHelper.<T>observableHandler();
    observable.subscribe(onNext, onError);
    return observable.asHandler();
  }

  /**
   * Adapt an item callback and an error callback as a {@code Handler<AsyncResult<T>>}.
   *
   * @param onNext the {@code Action1<T>} you have designed to accept the resolution from the {@code Handler<AsyncResult<T>>}
   * @param onError the {@code Action1<Throwable>} you have designed to accept the eventual failure from the {@code Handler<AsyncResult<T>>}
   * @param onComplete the {@code Action0} you have designed to accept a completion notification from the {@code Handler<AsyncResult<T>>}
   * @return a {@code Handler<AsyncResult<T>>}
   */
  public static <T> Handler<AsyncResult<T>> toHandler(Action1<T> onNext, Action1<Throwable> onError, Action0 onComplete) {
    ObservableHandler<T> observable = RxHelper.<T>observableHandler();
    observable.subscribe(onNext, onError, onComplete);
    return observable.asHandler();
  }
}
