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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Response;

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
import com.thinkberg.moxo.dav.ResourceManager;
import com.thinkberg.moxo.dav.UnlockHandler;
import com.thinkberg.moxo.dav.WebdavHandler;

/**
 * @author Matthias L. Jugel
 * @author Stian Soiland
 * @version $Id$
 */
public abstract class MoxoWebdavServlet extends HttpServlet {
	private final Map<String, WebdavHandler> handlers = new HashMap<String, WebdavHandler>();

	public MoxoWebdavServlet() {
		createHandlers();
	}

	protected void createHandlers() {
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
			handler.setResourceManager(getResourceManager());
		}
	}

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String method = request.getMethod();
		log(">> " + request.getMethod() + " " + request.getPathInfo());
		if (request.getHeader("X-Litmus") != null) {
			log("!! " + request.getHeader("X-Litmus"));
		}
		WebdavHandler handler = handlers.get(method);
		if (handler != null) {
			handler.service(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		if (response instanceof Response) {
			Response jettyResponse = ((Response) response);
			String msg = "<< " + jettyResponse.getStatus();
			String reason = jettyResponse.getReason();
			if (reason != null) {
				msg += ": " + reason;
			}
			log(msg);
		}
	}

	public abstract ResourceManager getResourceManager();
}
