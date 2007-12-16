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
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class UnlockHandler extends WebdavHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = ResourceManager.getFileObject(request.getPathInfo());
    String lockTokenHeader = request.getHeader("Lock-Token");
    String lockToken = lockTokenHeader.substring(1, lockTokenHeader.length() - 1);
    log("UNLOCK(" + lockToken + ")");

    if (LockManager.getInstance().releaseLock(object, lockToken)) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}