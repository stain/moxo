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

package com.thinkberg.moxo.vfs.s3.jets3t;

import com.thinkberg.moxo.vfs.s3.S3FileName;
import com.thinkberg.moxo.vfs.s3.S3FileProvider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;

import java.util.Collection;

/**
 * An S3 file system.
 *
 * @author Matthias L. Jugel
 */
public class Jets3tFileSystem extends AbstractFileSystem {
  private S3Service service;
  private S3Bucket bucket;

  public Jets3tFileSystem(S3FileName fileName, FileSystemOptions fileSystemOptions) throws FileSystemException {
    super(fileName, null, fileSystemOptions);
    String bucketId = fileName.getRootFile();
    try {
      service = Jets3tConnector.getInstance().getService();
      bucket = new S3Bucket(bucketId);
      if (!service.isBucketAccessible(bucketId)) {
        bucket = service.createBucket(bucketId);
      }
      System.err.println("Created new S3 FileSystem: " + bucketId);
    } catch (S3ServiceException e) {
      throw new FileSystemException(e);
    }
  }

  @SuppressWarnings({"unchecked"})
  protected void addCapabilities(Collection caps) {
    caps.addAll(S3FileProvider.capabilities);
  }

  protected FileObject createFile(FileName fileName) throws Exception {
    return new Jets3tFileObject(fileName, this, service, bucket);
  }
}
