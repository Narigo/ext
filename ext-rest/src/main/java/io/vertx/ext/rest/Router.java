/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.rest;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.rest.impl.RouterImpl;

import java.util.List;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Router extends Handler<RoutingContext> {

  static Router router() {
    return new RouterImpl();
  }

  void accept(HttpServerRequest request);

  Route route();

  Route route(HttpMethod method, String path);

  Route route(String path);

  Route routeWithRegex(HttpMethod method, String regex);

  Route routeWithRegex(String regex);

  List<Route> getRoutes();

  Router clear();

}
