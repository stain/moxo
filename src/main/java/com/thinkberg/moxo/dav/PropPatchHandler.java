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
import com.thinkberg.moxo.dav.data.DavResource;
import com.thinkberg.moxo.dav.lock.LockException;
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Handle PROPPATCH requests. This currently a dummy only and will return a
 * forbidden status for any attempt to modify or remove a property.
 *
 * @author Matthias L. Jugel
 */
public class PropPatchHandler extends WebdavHandler {
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = ResourceManager.getFileObject(request.getPathInfo());

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

    SAXReader saxReader = new SAXReader();
    try {
      Document propDoc = saxReader.read(request.getInputStream());
//      log(propDoc);

      response.setContentType("text/xml");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(SC_MULTI_STATUS);

      if (object.exists()) {
        Document resultDoc = DocumentHelper.createDocument();
        Element multiStatusResponse = resultDoc.addElement("multistatus", "DAV:");
        Element responseEl = multiStatusResponse.addElement("response");
        try {
          URL url = new URL(getBaseUrl(request), URLEncoder.encode(object.getName().getPath(), "UTF-8"));
          log("!! " + url);
          responseEl.addElement("href").addText(url.toExternalForm());
        } catch (Exception e) {
          e.printStackTrace();
        }

        Element propstatEl = responseEl.addElement("propstat");
        Element propEl = propstatEl.addElement("prop");

        Element propertyUpdateEl = propDoc.getRootElement();
        for (Object elObject : propertyUpdateEl.elements()) {
          Element el = (Element) elObject;
          if ("set".equals(el.getName())) {
            for (Object propObject : el.elements()) {
              setProperty(propEl, object, (Element) propObject);
            }
          } else if ("remove".equals(el.getName())) {
            for (Object propObject : el.elements()) {
              removeProperty(propEl, object, (Element) propObject);
            }
          }
        }
        propstatEl.addElement("status").addText(DavResource.STATUS_403);

//        log(resultDoc);

        // write the actual response
        XMLWriter writer = new XMLWriter(response.getWriter(), OutputFormat.createCompactFormat());
        writer.write(resultDoc);
        writer.flush();
        writer.close();
      } else {
        log("!! " + object.getName().getPath() + " NOT FOUND");
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (DocumentException e) {
      log("!! inavlid request: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void setProperty(Element root, FileObject object, Element el) {
    List propList = el.elements();
    for (Object propElObject : propList) {
      Element propEl = (Element) propElObject;
      for (int i = 0; i < propEl.nodeCount(); i++) {
        propEl.node(i).detach();
      }
      root.add(propEl.detach());
    }
  }

  private void removeProperty(Element root, FileObject object, Element el) {
    setProperty(root, object, el);
  }


}
