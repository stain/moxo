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
public abstract class CopyMoveBase extends WebdavHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean overwrite = getOverwrite(request);
    FileObject object = getResourceManager().getFileObject(request.getPathInfo());
    FileObject targetObject = getDestination(request);

    try {
      // check that we can write the target
      LockManager.getInstance().checkCondition(targetObject, getIf(request));
      // if we move, check that we can actually write on the source
      if ("MOVE".equals(request.getMethod())) {
        LockManager.getInstance().checkCondition(object, getIf(request));
      }
    } catch (LockException e) {
      if (e.getLocks() != null) {
        response.sendError(SC_LOCKED);
      } else {
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      }
      return;
    }


    if (null == targetObject) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (object.equals(targetObject)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    if (targetObject.exists()) {
      if (!overwrite) {
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
        return;
      }
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      FileObject targetParent = targetObject.getParent();
      if (!targetParent.exists() ||
              !FileType.FOLDER.equals(targetParent.getType())) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
      }
      response.setStatus(HttpServletResponse.SC_CREATED);
    }

    copyOrMove(object, targetObject, getDepth(request));
  }

  protected abstract void copyOrMove(FileObject object, FileObject target, int depth) throws FileSystemException;

}
