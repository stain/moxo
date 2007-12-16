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

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * Encode a URL but leave some special characters in plain text.
 *
 * @author Matthias L. Jugel
 */
class URLEncoder {

  private static BitSet keepPlain;

  static {
    keepPlain = new BitSet(256);
    int i;
    for (i = 'a'; i <= 'z'; i++) {
      keepPlain.set(i);
    }
    for (i = 'A'; i <= 'Z'; i++) {
      keepPlain.set(i);
    }
    for (i = '0'; i <= '9'; i++) {
      keepPlain.set(i);
    }
    keepPlain.set('+');
    keepPlain.set('-');
    keepPlain.set('_');
    keepPlain.set('.');
    keepPlain.set('*');
    keepPlain.set('/');
    keepPlain.set(':');
  }


  @SuppressWarnings({"SameParameterValue"})
  public static String encode(String s, String enc) throws UnsupportedEncodingException {
    byte[] buf = s.getBytes(enc);
    StringBuffer result = new StringBuffer();
    for (byte aBuf : buf) {
      int c = (int) aBuf;
      if (keepPlain.get(c & 0xFF)) {
        result.append((char) c);
      } else {
        result.append('%').append(Integer.toHexString(c & 0xFF).toUpperCase());
      }
    }
    return result.toString();
  }
}
