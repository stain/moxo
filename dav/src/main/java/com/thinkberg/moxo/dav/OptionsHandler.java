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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class OptionsHandler extends WebdavHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setHeader("DAV", "1, 2");

    String path = request.getPathInfo();
    StringBuffer options = new StringBuffer();
    FileObject object = getResourceManager().getFileObject(path);
    if (object.exists()) {
      options.append("OPTIONS, GET, HEAD, POST, DELETE, TRACE, COPY, MOVE, LOCK, UNLOCK, PROPFIND");
      if (FileType.FOLDER.equals(object.getType())) {
        options.append(", PUT");
      }
    } else {
      options.append("OPTIONS, MKCOL, PUT, LOCK");
    }
    response.setHeader("Allow", options.toString());

    // see: http://www-128.ibm.com/developerworks/rational/library/2089.html
    response.setHeader("MS-Author-Via", "DAV");
  }
}
