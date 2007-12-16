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

package com.thinkberg.moxo.dav.lock;

import java.util.List;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class LockException extends Exception {
  private final List<Lock> locks;

  public LockException(List<Lock> locks) {
    super();
    this.locks = locks;
  }

  public List<Lock> getLocks() {
    return locks;
  }
}
