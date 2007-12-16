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
import com.thinkberg.moxo.dav.lock.Lock;
import com.thinkberg.moxo.dav.lock.LockConflictException;
import com.thinkberg.moxo.dav.lock.LockException;
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * Handle WebDAV LOCK requests.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class LockHandler extends WebdavHandler {
  private static final String TAG_LOCKSCOPE = "lockscope";
  private static final String TAG_LOCKTYPE = "locktype";
  private static final String TAG_OWNER = "owner";
  private static final String TAG_HREF = "href";
  private static final String TAG_PROP = "prop";
  private static final String TAG_LOCKDISCOVERY = "lockdiscovery";

  private static final String HEADER_LOCK_TOKEN = "Lock-Token";

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = ResourceManager.getFileObject(request.getPathInfo());

    try {
      Lock lock = LockManager.getInstance().checkCondition(object, getIf(request));
      if (lock != null) {
        sendLockAcquiredResponse(response, lock);
        return;
      }
    } catch (LockException e) {
      // handle locks below
    }

    try {
      SAXReader saxReader = new SAXReader();
      Document lockInfo = saxReader.read(request.getInputStream());
      //log(lockInfo);

      Element rootEl = lockInfo.getRootElement();
      String lockScope = null, lockType = null;
      Object owner = null;
      Iterator elIt = rootEl.elementIterator();
      while (elIt.hasNext()) {
        Element el = (Element) elIt.next();
        if (TAG_LOCKSCOPE.equals(el.getName())) {
          lockScope = el.selectSingleNode("*").getName();
        } else if (TAG_LOCKTYPE.equals(el.getName())) {
          lockType = el.selectSingleNode("*").getName();
        } else if (TAG_OWNER.equals(el.getName())) {
          // TODO correctly handle owner
          Node subEl = el.selectSingleNode("*");
          if (subEl != null && TAG_HREF.equals(subEl.getName())) {
            owner = new URL(el.selectSingleNode("*").getText());
          } else {
            owner = el.getText();
          }
        }
      }

      log("LOCK(" + lockType + ", " + lockScope + ", " + owner + ")");

      Lock requestedLock = new Lock(object, lockType, lockScope, owner, getDepth(request), getTimeout(request));
      try {
        LockManager.getInstance().acquireLock(requestedLock);
        sendLockAcquiredResponse(response, requestedLock);
      } catch (LockConflictException e) {
        response.sendError(SC_LOCKED);
      } catch (IllegalArgumentException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (DocumentException e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void sendLockAcquiredResponse(HttpServletResponse response, Lock lock) throws IOException {
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    response.setHeader(HEADER_LOCK_TOKEN, "<" + lock.getToken() + ">");

    Document propDoc = DocumentHelper.createDocument();
    Element propEl = propDoc.addElement(TAG_PROP, "DAV:");
    Element lockdiscoveryEl = propEl.addElement(TAG_LOCKDISCOVERY);

    lock.serializeToXml(lockdiscoveryEl);

    XMLWriter xmlWriter = new XMLWriter(response.getWriter());
    xmlWriter.write(propDoc);
    log(propDoc);
  }
}
