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

import com.thinkberg.moxo.vfs.s3.jets3t.Jets3tFileSystem;

import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * @author Matthias L. Jugel
 */
public class S3FileSystemFactory {
  public static FileSystem getFileSystem(S3FileName fileName, FileSystemOptions fileSystemOptions) throws FileSystemException {
    // TODO load dynamically
    return new Jets3tFileSystem(fileName, fileSystemOptions);
  }
}
