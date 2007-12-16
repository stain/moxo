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

package com.thinkberg.moxo.dav;

import com.thinkberg.moxo.ResourceManager;
import com.thinkberg.moxo.dav.lock.LockException;
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.mortbay.jetty.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class DeleteHandler extends WebdavHandler {

  private final static FileSelector ALL_FILES_SELECTOR = new FileSelector() {
    public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
      return true;
    }

    public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
      return true;
    }
  };

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = ResourceManager.getFileObject(request.getPathInfo());
    if (request instanceof Request) {
      String fragment = ((Request) request).getUri().getFragment();
      if (fragment != null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
    }

    try {
      LockManager.getInstance().checkCondition(object, getIf(request));
    } catch (LockException e) {
      if (e.getLocks() != null) {
        response.sendError(SC_LOCKED);
      } else {
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      }
      return;
    }

    if (object.exists()) {
      if (object.delete(ALL_FILES_SELECTOR) > 0) {
        response.setStatus(HttpServletResponse.SC_OK);
      } else {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
