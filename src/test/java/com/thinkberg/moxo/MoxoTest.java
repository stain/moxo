package com.thinkberg.moxo;

import com.thinkberg.moxo.dav.DavLockManagerTest;
import com.thinkberg.moxo.dav.DavResourceTest;
import com.thinkberg.moxo.vfs.S3FileNameTest;
import com.thinkberg.moxo.vfs.S3FileProviderTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jets3t.service.Jets3tProperties;

/**
 * Unit tests for the Moxo S3 Amazon Proxy server.
 *
 * @author Matthias L. Jugel
 */
public class MoxoTest extends TestCase {
  /**
   * The complete Moxo Test suite.
   *
   * @param name name of the test case
   */
  public MoxoTest(String name) {
    super(name);
  }

  /**
   * Add all known tests to the suite.
   *
   * @return the suite of tests being tested
   */
  public static Test suite() {
    TestSuite s = new TestSuite();

    s.addTestSuite(DavResourceTest.class);
    s.addTestSuite(DavLockManagerTest.class);

    String propertiesFileName = System.getProperty("moxo.properties", "moxo.properties");
    Jets3tProperties properties = Jets3tProperties.getInstance(propertiesFileName);

    String bucketId = properties.getStringProperty("bucket", null);
    if (null != bucketId) {
      s.addTestSuite(S3FileNameTest.class);
      s.addTestSuite(S3FileProviderTest.class);
    }

    return s;
  }


}
