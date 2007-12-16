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

import com.thinkberg.moxo.dav.lock.LockException;
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class PutHandler extends WebdavHandler {
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = getResourceManager().getFileObject(request.getPathInfo());

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

    // it is forbidden to write data on a folder
    if (object.exists() && FileType.FOLDER.equals(object.getType())) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    FileObject parent = object.getParent();
    if (!parent.exists()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    if (!FileType.FOLDER.equals(parent.getType())) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
      return;
    }

    InputStream is = request.getInputStream();
    OutputStream os = object.getContent().getOutputStream();
    log("PUT sends " + request.getHeader("Content-length") + " bytes");
    log("PUT copied " + Util.copyStream(is, os) + " bytes");
    os.flush();
    object.close();

    response.setStatus(HttpServletResponse.SC_CREATED);
  }
}
