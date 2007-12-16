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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.thinkberg.moxo.dav.data.DavResource;
import com.thinkberg.moxo.dav.data.DavResourceFactory;
import com.thinkberg.moxo.vfs.extensions.DepthFileSelector;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class PropFindHandler extends WebdavHandler {
  private static final String TAG_PROP = "prop";
  private static final String TAG_ALLPROP = "allprop";
  private static final String TAG_PROPNAMES = "propnames";
  private static final String TAG_MULTISTATUS = "multistatus";
  private static final String TAG_HREF = "href";
  private static final String TAG_RESPONSE = "response";

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    SAXReader saxReader = new SAXReader();
    try {
      Document propDoc = saxReader.read(request.getInputStream());
      // log(propDoc);
      Element propFindEl = propDoc.getRootElement();
      Element propEl = (Element) propFindEl.elementIterator().next();
      String propElName = propEl.getName();

      List<String> requestedProperties = new ArrayList<String>();
      boolean ignoreValues = false;
      if (TAG_PROP.equals(propElName)) {
        for (Object id : propEl.elements()) {
          requestedProperties.add(((Element) id).getName());
        }
      } else if (TAG_ALLPROP.equals(propElName)) {
        requestedProperties = DavResource.ALL_PROPERTIES;
      } else if (TAG_PROPNAMES.equals(propElName)) {
        requestedProperties = DavResource.ALL_PROPERTIES;
        ignoreValues = true;
      }

      FileObject object = getResourceManager().getFileObject(request.getPathInfo());
      if (object.exists()) {
        // respond as XML encoded multi status
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(SC_MULTI_STATUS);

        Document multiStatusResponse =
                getMultiStatusRespons(object,
                                      requestedProperties,
                                      getBaseUrl(request),
                                      getDepth(request),
                                      ignoreValues);
        //log(multiStatusResponse);

        // write the actual response
        XMLWriter writer = new XMLWriter(response.getWriter(), OutputFormat.createCompactFormat());
        writer.write(multiStatusResponse);
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

  private Document getMultiStatusRespons(FileObject object,
                                         List<String> requestedProperties,
                                         URL baseUrl,
                                         int depth,
                                         boolean ignoreValues) throws FileSystemException {
    Document propDoc = DocumentHelper.createDocument();
    propDoc.setXMLEncoding("UTF-8");

    Element multiStatus = propDoc.addElement(TAG_MULTISTATUS, "DAV:");
    FileObject[] children = object.findFiles(new DepthFileSelector(depth));
    for (FileObject child : children) {
      Element responseEl = multiStatus.addElement(TAG_RESPONSE);
      try {
        URL url = new URL(baseUrl, URLEncoder.encode(child.getName().getPath(), "UTF-8"));
        log("!! " + url);
        responseEl.addElement(TAG_HREF).addText(url.toExternalForm());
      } catch (Exception e) {
        e.printStackTrace();
      }
      DavResource resource = DavResourceFactory.getInstance().getDavResource(child);
      resource.setIgnoreValues(ignoreValues);
      resource.serializeToXml(responseEl, requestedProperties);
    }
    return propDoc;
  }
}
