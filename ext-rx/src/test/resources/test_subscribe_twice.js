var test = require("test");
var Rx = require("vertx-js/rx");
var eb = vertx.eventBus();
var consumer = eb.localConsumer("the-address");
var observer1 = Rx.Observer.create(
  function (evt) {
    test.fail();
  },
  function (err) {
    test.fail(err);
  },
  function () {
    test.fail();
  }
);
var observer2 = Rx.Observer.create(
  function (evt) {
    test.fail();
  },
  function (err) {
    test.testComplete();
  },
  function () {
    test.testComplete();
  }
);
var observable = Rx.toObservable(consumer);
observable.subscribe(observer1);
observable.subscribe(observer2);
