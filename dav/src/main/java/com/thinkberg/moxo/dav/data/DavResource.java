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

import com.thinkberg.moxo.dav.Util;
import com.thinkberg.moxo.dav.lock.Lock;
import com.thinkberg.moxo.dav.lock.LockManager;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
@SuppressWarnings({"SameReturnValue"})
public class DavResource extends AbstractDavResource {

  // @see http://www.webdav.org/specs/rfc2518.html#dav.properties
  public static final String PROP_CREATION_DATE = "creationdate";
  public static final String PROP_DISPLAY_NAME = "displayname";
  private static final String PROP_GET_CONTENT_LANGUAGE = "getcontentlanguage";
  private static final String PROP_GET_CONTENT_LENGTH = "getcontentlength";
  private static final String PROP_GET_CONTENT_TYPE = "getcontenttype";
  private static final String PROP_GET_ETAG = "getetag";
  private static final String PROP_GET_LAST_MODIFIED = "getlastmodified";
  private static final String PROP_LOCK_DISCOVERY = "lockdiscovery";
  public static final String PROP_RESOURCETYPE = "resourcetype";
  private static final String PROP_SOURCE = "source";
  private static final String PROP_SUPPORTED_LOCK = "supportedlock";

  // non-standard properties
  static final String PROP_QUOTA = "quota";
  static final String PROP_QUOTA_USED = "quotaused";
  static final String PROP_QUOTA_AVAILABLE_BYTES = "quota-available-bytes";
  static final String PROP_QUOTA_USED_BYTES = "quota-used-bytes";

  // list of standard supported properties (for allprop/propname)
  public static final List<String> ALL_PROPERTIES = Arrays.asList(
          PROP_CREATION_DATE,
          PROP_DISPLAY_NAME,
          PROP_GET_CONTENT_LANGUAGE,
          PROP_GET_CONTENT_LENGTH,
          PROP_GET_CONTENT_TYPE,
          PROP_GET_ETAG,
          PROP_GET_LAST_MODIFIED,
          PROP_LOCK_DISCOVERY,
          PROP_RESOURCETYPE,
          PROP_SOURCE,
          PROP_SUPPORTED_LOCK
  );

  private final FileObject object;
  private boolean ignoreValues = false;

  public DavResource(FileObject object) {
    this(object, false);
  }


  public DavResource(FileObject object, boolean ignoreValues) {
    this.object = object;
    this.ignoreValues = ignoreValues;

  }

  /**
   * Ignore values
   *
   * @param ignoreValues true if the serialized xml should not contain values
   */
  public void setIgnoreValues(boolean ignoreValues) {
    this.ignoreValues = ignoreValues;
  }

  /**
   * Add the value for a given property to the result document. If the value
   * is missing or can not be added for some reason it will return false to
   * indicate a missing property.
   *
   * @param root         the root element for the result document fragment
   * @param propertyName the property name to add
   * @return true for successful addition and false for missing data
   */
  protected boolean addPropertyValue(Element root, String propertyName) {
    if (PROP_CREATION_DATE.equals(propertyName)) {
      return addCreationDateProperty(root);
    } else if (PROP_DISPLAY_NAME.equals(propertyName)) {
      return addGetDisplayNameProperty(root);
    } else if (PROP_GET_CONTENT_LANGUAGE.equals(propertyName)) {
      return addGetContentLanguageProperty(root);
    } else if (PROP_GET_CONTENT_LENGTH.equals(propertyName)) {
      return addGetContentLengthProperty(root);
    } else if (PROP_GET_CONTENT_TYPE.equals(propertyName)) {
      return addGetContentTypeProperty(root);
    } else if (PROP_GET_ETAG.equals(propertyName)) {
      return addGetETagProperty(root);
    } else if (PROP_GET_LAST_MODIFIED.equals(propertyName)) {
      return addGetLastModifiedProperty(root);
    } else if (PROP_LOCK_DISCOVERY.equals(propertyName)) {
      return addLockDiscoveryProperty(root);
    } else if (PROP_RESOURCETYPE.equals(propertyName)) {
      return addResourceTypeProperty(root);
    } else if (PROP_SOURCE.equals(propertyName)) {
      return addSourceProperty(root);
    } else if (PROP_SUPPORTED_LOCK.equals(propertyName)) {
      return addSupportedLockProperty(root);
    } else {
      // handle non-standard properties (keep a little separate)
      if (PROP_QUOTA.equals(propertyName)) {
        return addQuotaProperty(root);
      } else if (PROP_QUOTA_USED.equals(propertyName)) {
        return addQuotaUsedProperty(root);
      } else if (PROP_QUOTA_AVAILABLE_BYTES.equals(propertyName)) {
        return addQuotaAvailableBytesProperty(root);
      } else if (PROP_QUOTA_USED_BYTES.equals(propertyName)) {
        return addQuotaUsedBytesProperty(root);
      }
    }

    return false;
  }

  @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
  protected boolean addCreationDateProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addGetDisplayNameProperty(Element root) {
    Element el = root.addElement(PROP_DISPLAY_NAME);
    if (!ignoreValues) {
      el.addCDATA(object.getName().getBaseName());
    }
    return true;
  }

  @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
  protected boolean addGetContentLanguageProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addGetContentLengthProperty(Element root) {
    try {
      Element el = root.addElement(PROP_GET_CONTENT_LENGTH);
      if (!ignoreValues) {
        el.addText("" + object.getContent().getSize());
      }
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
      return false;
    }
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addGetContentTypeProperty(Element root) {
    try {
      String contentType = object.getContent().getContentInfo().getContentType();
      if (null == contentType || "".equals(contentType)) {
        return false;
      }

      Element el = root.addElement(PROP_GET_CONTENT_TYPE);
      if (!ignoreValues) {
        el.addText(contentType);
      }
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
      return false;
    }
  }

  @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
  protected boolean addGetETagProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addGetLastModifiedProperty(Element root) {
    try {
      Element el = root.addElement(PROP_GET_LAST_MODIFIED);
      if (!ignoreValues) {
        el.addText(Util.getDateString(object.getContent().getLastModifiedTime()));
      }
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
      return false;
    }
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addLockDiscoveryProperty(Element root) {
    Element lockdiscoveryEl = root.addElement(PROP_LOCK_DISCOVERY);
    try {
      List<Lock> locks = LockManager.getInstance().discoverLock(object);
      if (locks != null && !locks.isEmpty()) {
        for (Lock lock : locks) {
          if (lock != null && !ignoreValues) {
            lock.serializeToXml(lockdiscoveryEl);
          }
        }
      }
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
      root.remove(lockdiscoveryEl);
      return false;
    }
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addResourceTypeProperty(Element root) {
    root.addElement(PROP_RESOURCETYPE);
    return true;
  }

  @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
  protected boolean addSourceProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addSupportedLockProperty(Element root) {
    Element supportedlockEl = root.addElement(PROP_SUPPORTED_LOCK);
    if (!ignoreValues) {
      Element exclLockentryEl = supportedlockEl.addElement("lockentry");
      exclLockentryEl.addElement("lockscope").addElement("exclusive");
      exclLockentryEl.addElement("locktype").addElement("write");
      Element sharedLockentryEl = supportedlockEl.addElement("lockentry");
      sharedLockentryEl.addElement("lockscope").addElement("shared");
      sharedLockentryEl.addElement("locktype").addElement("write");
    }

    return true;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addQuotaProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addQuotaUsedProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addQuotaAvailableBytesProperty(Element root) {
    return false;
  }

  @SuppressWarnings({"WeakerAccess"})
  protected boolean addQuotaUsedBytesProperty(Element root) {
    return false;
  }
}
