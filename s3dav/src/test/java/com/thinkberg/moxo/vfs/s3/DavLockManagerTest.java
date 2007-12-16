package com.thinkberg.moxo.vfs.s3;

import com.thinkberg.moxo.dav.lock.Lock;
import com.thinkberg.moxo.dav.lock.LockConflictException;
import com.thinkberg.moxo.dav.lock.LockManager;

/**
 * @author Matthias L. Jugel
 */
public class DavLockManagerTest extends DavTestCase {
  private final String OWNER_STR = "testowner";

  public DavLockManagerTest() {
    super();
  }

  public void testAcquireSharedFileLock() {
    Lock sharedLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
    try {
      LockManager.getInstance().acquireLock(sharedLock);
    } catch (Exception e) {
      assertNull(e.getMessage(), e);
    }
  }

  public void testAcquireDoubleSharedFileLock() {
    Lock sharedLock1 = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
    Lock sharedLock2 = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR + "1", 0, 3600);
    try {
      LockManager.getInstance().acquireLock(sharedLock1);
      LockManager.getInstance().acquireLock(sharedLock2);
    } catch (Exception e) {
      assertNull(e.getMessage(), e);
    }
  }

  public void testAcquireExclusiveLock() {
    Lock sharedLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
    Lock exclusiveLock = new Lock(aFile, Lock.WRITE, Lock.EXCLUSIVE, OWNER_STR, 0, 3600);
    try {
      LockManager.getInstance().acquireLock(sharedLock);
      LockManager.getInstance().acquireLock(exclusiveLock);
      assertTrue("acquireLock() should fail", false);
    } catch (Exception e) {
      assertEquals(LockConflictException.class, e.getClass());
    }
  }
}
