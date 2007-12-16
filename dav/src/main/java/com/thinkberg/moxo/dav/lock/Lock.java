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

package com.thinkberg.moxo.dav.lock;

import org.apache.commons.vfs.FileObject;
import org.dom4j.Element;

import java.net.URL;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class Lock {
  public final static String SHARED = "shared";
  public final static String EXCLUSIVE = "exclusive";

  public final static String WRITE = "write";

  private final FileObject object;
  private final String type;
  private final String scope;
  private final Object owner;
  private final int depth;
  private final long timeout;
  private final String token;


  public Lock(FileObject object, String type, String scope, Object owner,
              int depth, long timeout) {
    this.object = object;
    this.type = type;
    this.scope = scope;
    this.owner = owner;
    this.depth = depth;
    this.timeout = timeout;

    this.token = "opaquelocktoken:" + Integer.toHexString(object.hashCode());
  }

  public FileObject getObject() {
    return object;
  }

  public String getType() {
    return type;
  }

  public String getScope() {
    return scope;
  }

  public Object getOwner() {
    return owner;
  }

  public int getDepth() {
    return depth;
  }

  @SuppressWarnings({"WeakerAccess"})
  public String getDepthValue() {
    switch (depth) {
      case 0:
        return "0";
      case 1:
        return "1";
      default:
        return "Infinity";
    }
  }

  @SuppressWarnings({"WeakerAccess"})
  public String getTimeout() {
    if (timeout == -1) {
      return "Infinity";
    }
    return "Second-" + timeout;
  }

  public String getToken() {
    return this.token;
  }

  /**
   * Create an XML serialized version of the lock by adding an activelock tag
   * with the locks properties to the root element provided.
   *
   * @param root the root element to add the activelock to
   * @return the root element
   */
  @SuppressWarnings({"UnusedReturnValue"})
  public Element serializeToXml(Element root) {
    Element activelockEl = root.addElement("activelock");
    activelockEl.addElement("locktype").addElement(getType());
    activelockEl.addElement("lockscope").addElement(getScope());
    activelockEl.addElement("depth").addText(getDepthValue());
    // TODO handle owner correctly
    if (getOwner() instanceof URL) {
      activelockEl.addElement("owner").addElement("href")
              .addText(((URL) getOwner()).toExternalForm());
    } else {
      activelockEl.addElement("owner").addText((String) getOwner());
    }
    activelockEl.addElement("timeout").addText(getTimeout());
    activelockEl.addElement("locktoken")
            .addElement("href").addText(getToken());

    return root;
  }

  /**
   * There can only be one lock per object, thus compare the file objects.
   *
   * @param other the other lock to compare to
   * @return whether this lock is for the same file object
   */
  public boolean equals(Object other) {
    return object.equals(((Lock) other).object);
  }


  public String toString() {
    return new StringBuffer().append("Lock[")
            .append(object).append(",")
            .append(type).append(",")
            .append(scope).append("]").toString();
  }
}

