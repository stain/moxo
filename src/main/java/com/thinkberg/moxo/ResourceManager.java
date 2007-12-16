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

package com.thinkberg.moxo;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.jets3t.service.Jets3tProperties;

/**
 * The resource manager is responsible for providing a virtual file system root.
 *
 * @author Matthias L. Jugel
 */
public class ResourceManager {
  private static FileObject root;

  static {
    try {
      String propertiesFileName = System.getProperty("moxo.properties", "moxo.properties");
      Jets3tProperties properties = Jets3tProperties.getInstance(propertiesFileName);
      FileSystemManager fsm = VFS.getManager();
      FileObject rootObject = fsm.resolveFile("s3://" + properties.getStringProperty("bucket", null));
      root = fsm.createVirtualFileSystem(rootObject);
      System.err.println("Created virtual file system: " + rootObject);
    } catch (FileSystemException e) {
      System.err.println("Can't create virtual file system: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static FileObject getFileObject(String path) throws FileSystemException {
    return root.resolveFile(path);
  }
}
