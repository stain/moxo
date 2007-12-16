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
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class MkColHandler extends WebdavHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getReader().readLine() != null) {
      response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      return;
    }

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

    if (object.exists()) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    if (!object.getParent().exists() || !FileType.FOLDER.equals(object.getParent().getType())) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
      return;
    }

    try {
      object.createFolder();
      response.setStatus(HttpServletResponse.SC_CREATED);
    } catch (FileSystemException e) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
