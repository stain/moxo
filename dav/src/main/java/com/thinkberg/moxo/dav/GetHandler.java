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

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
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
public class GetHandler extends WebdavHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = getResourceManager().getFileObject(request.getPathInfo());

    if (object.exists()) {
      if (FileType.FOLDER.equals(object.getType())) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      setHeader(response, object.getContent());

      InputStream is = object.getContent().getInputStream();
      OutputStream os = response.getOutputStream();
      Util.copyStream(is, os);
      is.close();
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  void setHeader(HttpServletResponse response, FileContent content) throws FileSystemException {
    response.setHeader("Last-Modified", Util.getDateString(content.getLastModifiedTime()));
    response.setHeader("Content-Type", content.getContentInfo().getContentType());
  }


}
