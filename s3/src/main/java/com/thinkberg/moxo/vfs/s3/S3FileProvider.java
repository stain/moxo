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

package com.thinkberg.moxo.vfs.s3;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * An S3 file provider. Create an S3 file system out of an S3 file name.
 * Also defines the capabilities of the file system.
 *
 * @author Matthias L. Jugel
 */
public class S3FileProvider extends AbstractOriginatingFileProvider {

  public final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(
          Capability.CREATE,
          Capability.DELETE,
          Capability.RENAME,
          Capability.GET_TYPE,
          Capability.GET_LAST_MODIFIED,
          Capability.SET_LAST_MODIFIED_FILE,
          Capability.SET_LAST_MODIFIED_FOLDER,
          Capability.LIST_CHILDREN,
          Capability.READ_CONTENT,
          Capability.URI,
          Capability.WRITE_CONTENT,
          Capability.APPEND_CONTENT/*,
          Capability.RANDOM_ACCESS_READ,
          Capability.RANDOM_ACCESS_WRITE*/

  ));


  public S3FileProvider() {
    super();
    setFileNameParser(S3FileNameParser.getInstance());
  }

  /**
   * Create a file system with the S3 root provided.
   *
   * @param fileName          the S3 file name that defines the root (bucket)
   * @param fileSystemOptions file system options
   * @return an S3 file system
   * @throws FileSystemException if te file system cannot be created
   */
  protected FileSystem doCreateFileSystem(FileName fileName, FileSystemOptions fileSystemOptions) throws FileSystemException {
    return S3FileSystemFactory.getFileSystem((S3FileName) fileName, fileSystemOptions);
  }

  /**
   * Get the capabilities of the file system provider.
   *
   * @return the file system capabilities
   */
  public Collection getCapabilities() {
    return capabilities;
  }
}
