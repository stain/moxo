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

package com.thinkberg.moxo.dav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class Util {

  private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

  public static String getDateString(long time) {
    return httpDateFormat.format(new Date(time));
  }

//  public static String getISODateString(long time) {
//    return "";
//  }


  public static int copyStream(InputStream is, OutputStream os) throws IOException {
    byte[] buffer = new byte[8192];
    int bytesRead, bytesCount = 0;
    while ((bytesRead = is.read(buffer)) != -1) {
      os.write(buffer, 0, bytesRead);
      bytesCount += bytesRead;
    }
    os.flush();

    return bytesCount;
  }
}
