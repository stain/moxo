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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;

import com.thinkberg.moxo.vfs.extensions.DepthFileSelector;

/**
 * The lock manager is responsible for exclusive and shared write locks on the
 * DAV server. It is used to acquire a lock, release a lock, discover existing
 * locks or check conditions. The lock manager is a singleton.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class LockManager {
  private static LockManager instance = null;

  /**
   * Get an instance of the lock manager.
   *
   * @return the lock manager
   */
  public static LockManager getInstance() {
    if (null == instance) {
      instance = new LockManager();
    }

    return instance;
  }

  private final Map<FileObject, List<Lock>> lockMap;

  /**
   * The lock manager is a singleton and cannot be instantiated directly.
   */
  private LockManager() {
    lockMap = new HashMap<FileObject, List<Lock>>();
  }

  /**
   * Acquire a lock. This will first check for conflicts and throws exceptions if
   * there are existing locks or for some reason the lock could not be acquired.
   *
   * @param lock the lock to acquire
   * @throws LockConflictException if an existing lock has priority
   * @throws FileSystemException   if the file object and its path cannot be accessed
   */
  public void acquireLock(Lock lock) throws LockConflictException, FileSystemException {
    checkConflicts(lock);
    addLock(lock);
  }

  /**
   * Release a lock on a file object with a given lock token. Releeases the lock if
   * if one exists and if the lock token is valid for the found lock.
   *
   * @param object the file object we want to unlock
   * @param token  the lock token associated with the file object
   * @return true if the lock has been released, false if not
   */
  public boolean releaseLock(FileObject object, String token) {
    List<Lock> locks = lockMap.get(object);
    if (null != locks) {
      for (Lock lock : locks) {
        if (lock.getToken().equals(token)) {
          locks.remove(lock);
          return true;
        }
      }
      return false;
    }

    return true;
  }

  /**
   * Discover locks for a given file object. This will find locks for the object
   * itself and parent path locks with a depth that reaches the file object.
   *
   * @param object the file object to find locks for
   * @return the locks that are found for this file object
   * @throws FileSystemException if the file object or its parents cannot be accessed
   */
  public List<Lock> discoverLock(FileObject object) throws FileSystemException {
    FileObject parent = object;
    while (parent != null) {
      List<Lock> parentLocks = lockMap.get(parent);
      if (parentLocks != null && !parentLocks.isEmpty()) {
        return parentLocks;
      }
      parent = parent.getParent();
    }

    return null;
  }

  /**
   * Check a condition for a file object. The condition check looks for locks on the
   * given file object and will throw exceptions if the condition does not meet the
   * lock requirements (i.e. lock token in the condition differes from the token in
   * the discovered lock) or no condition exists if a lock was discovered. If no lock
   * was discovered but a condition exists a lock condition exception will be thrown.
   *
   * @param object the file object in question
   * @param ifCond the condition to check
   * @return the lock found for the given if condition and the object
   * @throws FileSystemException          if the object or path cannot be accessed
   * @throws LockConflictException        if there is a a lock but no condition
   * @throws LockConditionFailedException if the condition and lock does not match
   */
  public Lock checkCondition(FileObject object, String ifCond)
          throws FileSystemException, LockConflictException, LockConditionFailedException {
    List<Lock> locks = discoverLock(object);
    if (null != locks && !locks.isEmpty()) {
      // if there is no condition but a lock, this must fail
      if (null == ifCond) {
        throw new LockConflictException(locks);
      }

      // simple check whether the token is in the condition (TODO: check for NOT)
      for (Lock lock : locks) {
        if (ifCond.indexOf("<" + lock.getToken() + ">") != -1) {
          return lock;
        }
      }
      throw new LockConditionFailedException(locks);
    } else if (null != ifCond) {
      // no lock but a condition must fail too
      throw new LockConditionFailedException(null);
    }

    return null;
  }


  /**
   * Add a lock to the list of shared locks of a given object.
   *
   * @param lock the lock to add
   */
  private void addLock(Lock lock) {
    FileObject object = lock.getObject();
    List<Lock> locks = lockMap.get(object);
    if (null == locks) {
      locks = new ArrayList<Lock>();
      lockMap.put(object, locks);
    }
    locks.add(lock);
  }

  /**
   * Check whether a lock conflicts with already existing locks up and down the path.
   * First we go up the path to check for parent locks that may include the file object
   * and the go down the directory tree (if depth requires it) to check locks that
   * will conflict.
   *
   * @param requestedLock the lock requested
   * @throws LockConflictException if a conflicting lock was found
   * @throws FileSystemException   if the file object or path cannot be accessed
   */
  private void checkConflicts(final Lock requestedLock) throws LockConflictException, FileSystemException {
    // find locks in the parent path
    FileObject parent = requestedLock.getObject();
    while (parent != null) {
      List<Lock> parentLocks = lockMap.get(parent);
      if (parentLocks != null && !parentLocks.isEmpty()) {
        for (Lock parentLock : parentLocks) {
          if (Lock.EXCLUSIVE.equals(requestedLock.getScope()) || Lock.EXCLUSIVE.equals(parentLock.getScope())) {
            throw new LockConflictException(parentLocks);
          }
        }
      }
      parent = parent.getParent();
    }

    // look for locks down the path (if depth requests it)
    if (requestedLock.getDepth() != 0 && requestedLock.getObject().getChildren().length > 0) {
      requestedLock.getObject().findFiles(new DepthFileSelector(1, requestedLock.getDepth()) {
        public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
          List<Lock> childLocks = lockMap.get(fileSelectInfo.getFile());
          for (Lock childLock : childLocks) {
            if (Lock.EXCLUSIVE.equals(requestedLock.getScope()) || Lock.EXCLUSIVE.equals(childLock.getScope())) {
              throw new LockConflictException(childLocks);
            }
          }
          return false;
        }
      }, false, new ArrayList());
    }
  }
}
