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

import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

/**
 * @author Matthias L. Jugel
 */
public class Jets3tConnector {
  private static final String APPLICATION_DESCRIPTION = "S3 VFS Connector/1.0";

  private static Jets3tConnector instance;

  /**
   * Get an instance of the Jets3tConnector which is initialized and authenticated to the
   * Amazon S3 Service.
   *
   * @return a Jets3t S3 connector
   * @throws org.jets3t.service.S3ServiceException
   *          if connection or authentication fails
   */
  public static Jets3tConnector getInstance() throws S3ServiceException {
    if (null == instance) {
      instance = new Jets3tConnector();
    }
    return instance;
  }

  private S3Service service;

  /**
   * Initialize Amazon S3.
   *
   * @throws org.jets3t.service.S3ServiceException
   *          if the service cannot be accessed
   */
  private Jets3tConnector() throws S3ServiceException {
    System.err.print("Authenticated to Amazon S3: ");
    String propertiesFileName = System.getProperty("moxo.properties", "moxo.properties");
    Jets3tProperties properties = Jets3tProperties.getInstance(propertiesFileName);

    if (!properties.isLoaded()) {
      throw new S3ServiceException("can't find S3 configuration: " + propertiesFileName);
    }

    AWSCredentials awsCredentials = new AWSCredentials(
            properties.getStringProperty("accesskey", null),
            properties.getStringProperty("secretkey", null));


    service = new RestS3Service(awsCredentials, APPLICATION_DESCRIPTION, null);
    System.err.println("OK");
  }

  public S3Service getService() {
    return service;
  }
}
