/*
 * Copyright 2007 Matthias L. Jugel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thinkberg.moxo.servlet;

import com.thinkberg.moxo.dav.CopyHandler;
import com.thinkberg.moxo.dav.DeleteHandler;
import com.thinkberg.moxo.dav.GetHandler;
import com.thinkberg.moxo.dav.HeadHandler;
import com.thinkberg.moxo.dav.LockHandler;
import com.thinkberg.moxo.dav.MkColHandler;
import com.thinkberg.moxo.dav.MoveHandler;
import com.thinkberg.moxo.dav.OptionsHandler;
import com.thinkberg.moxo.dav.PostHandler;
import com.thinkberg.moxo.dav.PropFindHandler;
import com.thinkberg.moxo.dav.PropPatchHandler;
import com.thinkberg.moxo.dav.PutHandler;
import com.thinkberg.moxo.dav.UnlockHandler;
import com.thinkberg.moxo.dav.WebdavHandler;
import org.mortbay.jetty.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class MoxoS3WebdavServlet extends HttpServlet {
  private final Map<String, WebdavHandler> handlers = new HashMap<String, WebdavHandler>();

  public MoxoS3WebdavServlet() {
    handlers.put("COPY", new CopyHandler());
    handlers.put("DELETE", new DeleteHandler());
    handlers.put("GET", new GetHandler());
    handlers.put("HEAD", new HeadHandler());
    handlers.put("LOCK", new LockHandler());
    handlers.put("MKCOL", new MkColHandler());
    handlers.put("MOVE", new MoveHandler());
    handlers.put("OPTIONS", new OptionsHandler());
    handlers.put("POST", new PostHandler());
    handlers.put("PROPFIND", new PropFindHandler());
    handlers.put("PROPPATCH", new PropPatchHandler());
    handlers.put("PUT", new PutHandler());
    handlers.put("UNLOCK", new UnlockHandler());

    for (WebdavHandler handler : handlers.values()) {
      handler.setServlet(this);
    }
  }

  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String method = request.getMethod();
    log(">> " + request.getMethod() + " " + request.getPathInfo());
    if (request.getHeader("X-Litmus") != null) {
      log("!! " + request.getHeader("X-Litmus"));
    }
    if (handlers.containsKey(method)) {
      handlers.get(method).service(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    Response jettyResponse = ((Response) response);
    String reason = jettyResponse.getReason();
    log("<< " + jettyResponse.getStatus() + (reason != null ? ": " + reason : ""));
  }
}
