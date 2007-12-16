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

package com.thinkberg.moxo.dav.data;

import org.dom4j.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public abstract class AbstractDavResource {
  private static final String STATUS_200 = "HTTP/1.1 200 OK";
  private static final String STATUS_404 = "HTTP/1.1 404 Not Found";
  public static final String STATUS_403 = "HTTP/1.1 403 Forbidden";

  private static final String TAG_PROPSTAT = "propstat";
  private static final String TAG_PROP = "prop";
  private static final String TAG_STATUS = "status";

  @SuppressWarnings({"UnusedReturnValue"})
  public Element serializeToXml(Element root, List<String> requestedProperties) {
    Element propStatEl = root.addElement(TAG_PROPSTAT);
    Element propEl = propStatEl.addElement(TAG_PROP);

    Set<String> missingProperties = new HashSet<String>();
    for (String propertyName : requestedProperties) {
      if (!addPropertyValue(propEl, propertyName)) {
        missingProperties.add(propertyName);
      }
    }
    propStatEl.addElement(TAG_STATUS).addText(STATUS_200);

    // add missing properties status
    if (missingProperties.size() > 0) {
      propStatEl = root.addElement(TAG_PROPSTAT);
      propEl = propStatEl.addElement(TAG_PROP);
      for (String el : missingProperties) {
        propEl.addElement(el);
      }
      propStatEl.addElement(TAG_STATUS).addText(STATUS_404);
    }

    return root;
  }

  protected abstract boolean addPropertyValue(Element root, String propertyName);
}
