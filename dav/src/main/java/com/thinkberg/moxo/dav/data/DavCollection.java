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

import org.apache.commons.vfs.FileObject;
import org.dom4j.Element;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class DavCollection extends DavResource {
  public static final String COLLECTION = "collection";

  public DavCollection(FileObject object) {
    super(object);
  }


  public DavCollection(FileObject object, boolean ignoreValues) {
    super(object, ignoreValues);
  }

  protected boolean addResourceTypeProperty(Element root) {
    root.addElement(PROP_RESOURCETYPE).addElement(COLLECTION);
    return true;
  }

  /**
   * Ignore content language for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  protected boolean addGetContentLanguageProperty(Element root) {
    return true;
  }

  /**
   * Ignore content length for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  protected boolean addGetContentLengthProperty(Element root) {
    return true;
  }

  /**
   * Ignore content type for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  protected boolean addGetContentTypeProperty(Element root) {
    return true;
  }

  protected boolean addQuotaProperty(Element root) {
    root.addElement(PROP_QUOTA).addText("1000000");
    return true;
  }

  protected boolean addQuotaUsedProperty(Element root) {
    // TODO add correct handling of used quota
    root.addElement(PROP_QUOTA_USED).addText("0");
    return true;
  }

  protected boolean addQuotaAvailableBytesProperty(Element root) {
    root.addElement(PROP_QUOTA_AVAILABLE_BYTES).addText("1000000");
    return true;
  }

  protected boolean addQuotaUsedBytesProperty(Element root) {
    // TODO add correct handling of used quota
    root.addElement(PROP_QUOTA_USED_BYTES).addText("0");
    return true;
  }
}
