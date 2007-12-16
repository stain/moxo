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

import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Java Webserver starter uses jetty.
 *
 * @author Matthias L. Jugel
 */
public class MoxoJettyRunner {
  private static final String CONF_JETTY_XML = "jetty.xml";

  public static void main(String[] args) {
    System.out.println("Moxo S3 DAV Proxy (c) 2007 Matthias L. Jugel");

    // set encoding of the JVM and make sure Jetty decodes URIs correctly
    System.setProperty("file.encoding", "UTF-8");
    System.setProperty("org.mortbay.util.URI.charset", "UTF-8");

    try {
      Server server = new Server();
      XmlConfiguration xmlConfiguration = new XmlConfiguration(getResource(CONF_JETTY_XML));
      xmlConfiguration.configure(server);
      server.start();
      server.join();
    } catch (Exception e) {
      System.err.println("Can't start server: " + e.getMessage());
      System.exit(1);
    }
  }

  @SuppressWarnings({"SameParameterValue"})
  private static URL getResource(String resource) {
    URL url = MoxoJettyRunner.class.getResource("/" + resource);
    if (null == url) {
      try {
        url = new File(resource).toURL();
        System.err.println("Loading configuration from file: " + url.toExternalForm());
      } catch (MalformedURLException e) {
        // ignore ...
      }
    }
    return url;
  }
}
