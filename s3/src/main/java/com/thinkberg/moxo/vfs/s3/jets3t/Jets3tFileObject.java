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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.utils.Mimetypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;

/**
 * Implementation of the virtual S3 file system object using the Jets3t library.
 *
 * @author Matthias L. Jugel
 */
public class Jets3tFileObject extends AbstractFileObject {
  private final S3Service service;
  private final S3Bucket bucket;

  private boolean attached = false;
  private boolean changed = false;
  private S3Object object;
  private File cacheFile;

  public Jets3tFileObject(FileName fileName,
                          Jets3tFileSystem fileSystem,
                          S3Service service, S3Bucket bucket)
          throws FileSystemException {
    super(fileName, fileSystem);
    this.service = service;
    this.bucket = bucket;
  }

  protected void doAttach() throws Exception {
    if (!attached) {
      try {
        object = service.getObject(bucket, getS3Key());
        System.err.println("Attached file to S3 Object: " + object);
        InputStream is = object.getDataInputStream();
        if (object.getContentLength() > 0) {
          ReadableByteChannel rbc = Channels.newChannel(is);
          FileChannel cacheFc = getCacheFileChannel();
          cacheFc.transferFrom(rbc, 0, object.getContentLength());
          cacheFc.close();
          rbc.close();
        } else {
          is.close();
        }
      } catch (S3ServiceException e) {
        object = new S3Object(bucket, getS3Key());
        object.setLastModifiedDate(new Date());
        System.err.println("Attached file to new S3 Object: " + object);
      }
      attached = true;
    }
  }

  protected void doDetach() throws Exception {
    // TODO do not send immediately but put in some kind of upload queue
    if (attached && changed) {
      System.err.println("Detaching changed object: " + object);
      if (cacheFile != null) {
        FileChannel cacheFc = getCacheFileChannel();
        object.setContentLength(cacheFc.size());
        object.setDataInputStream(getInputStream());
      }
      System.err.println(object);
      service.putObject(bucket, object);
      attached = false;
    }
  }

  protected void doDelete() throws Exception {
    service.deleteObject(bucket, object.getKey());
  }

  protected void doRename(FileObject newfile) throws Exception {
    super.doRename(newfile);
  }

  protected void doCreateFolder() throws Exception {
    if (!Mimetypes.MIMETYPE_JETS3T_DIRECTORY.equals(object.getContentType())) {
      object.setContentType(Mimetypes.MIMETYPE_JETS3T_DIRECTORY);
      service.putObject(bucket, object);
      changed = false;
    }
  }

  protected long doGetLastModifiedTime() throws Exception {
    return object.getLastModifiedDate().getTime();
  }

  protected void doSetLastModifiedTime(final long modtime) throws Exception {
    changed = true;
    object.setLastModifiedDate(new Date(modtime));
  }

  protected InputStream doGetInputStream() throws Exception {
    return Channels.newInputStream(getCacheFileChannel());
  }

  protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
    changed = true;
    return Channels.newOutputStream(getCacheFileChannel());
  }

  protected FileType doGetType() throws Exception {
    if (null == object.getContentType()) {
      return FileType.IMAGINARY;
    }

    String contentType = object.getContentType();
    if ("".equals(object.getKey()) || Mimetypes.MIMETYPE_JETS3T_DIRECTORY.equals(contentType)) {
      return FileType.FOLDER;
    }

    return FileType.FILE;
  }

  protected String[] doListChildren() throws Exception {
    String path = object.getKey();
    // make sure we add a '/' slash at the end to find children
    if (!"".equals(path)) {
      path = path + "/";
    }

    S3Object[] children = service.listObjects(bucket, path, "/");
    String[] childrenNames = new String[children.length];
    for (int i = 0; i < children.length; i++) {
      if (!children[i].getKey().equals(path)) {
        // strip path from name (leave only base name)
        childrenNames[i] = children[i].getKey().replaceAll("[^/]*//*", "");
      }
    }
    return childrenNames;
  }

  protected long doGetContentSize() throws Exception {
    return object.getContentLength();
  }

  // Utility methods
  /**
   * Create an S3 key from a commons-vfs path. This simply
   * strips the slash from the beginning if it exists.
   *
   * @return the S3 object key
   */
  private String getS3Key() {
    String path = getName().getPath();
    if ("".equals(path)) {
      return path;
    } else {
      return path.substring(1);
    }
  }

  private FileChannel getCacheFileChannel() throws IOException {
    if (cacheFile == null) {
      cacheFile = File.createTempFile("moxo.", ".s3");
    }
    return new RandomAccessFile(cacheFile, "rw").getChannel();
  }
}
