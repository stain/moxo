package com.thinkberg.moxo.dav;

import com.thinkberg.moxo.dav.data.DavCollection;
import com.thinkberg.moxo.dav.data.DavResource;
import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Element;

import java.io.IOException;

/**
 * Test case for the DAV resource wrapper. Checks that resources are serialized
 * correctly.
 *
 * @author Matthias L. Jugel
 */
public class DavResourceTest extends DavTestCase {
  public void testFileCreationDateIsNull() throws FileSystemException {
    Element root = serializeDavResource(aFile, DavResource.PROP_CREATION_DATE);
    assertNull(selectExistingProperty(root, DavResource.PROP_CREATION_DATE));
  }

  public void testFileCreationDateIsMissing() throws IOException {
    Element root = serializeDavResource(aFile, DavResource.PROP_CREATION_DATE);
    assertEquals(DavResource.PROP_CREATION_DATE,
                 selectMissingPropertyName(root, DavResource.PROP_CREATION_DATE));
  }

  public void testFileDisplayNameWithValue() throws FileSystemException {
    testPropertyValue(aFile, DavResource.PROP_DISPLAY_NAME, aFile.getName().getBaseName());
  }

  public void testFileDisplayNameWithoutValue() throws FileSystemException {
    testPropertyNoValue(aFile, DavResource.PROP_DISPLAY_NAME);
  }

  public void testFileResourceTypeNotMissing() throws FileSystemException {
    Element root = serializeDavResource(aFile, DavResource.PROP_RESOURCETYPE);
    assertNull(selectMissingProperty(root, DavResource.PROP_RESOURCETYPE));
  }

  public void testDirectoryResourceTypeNotMissing() throws FileSystemException {
    Element root = serializeDavResource(aDirectory, DavResource.PROP_RESOURCETYPE);
    assertNull(selectMissingProperty(root, DavResource.PROP_RESOURCETYPE));
  }

  public void testFileResourceType() throws FileSystemException {
    testPropertyNoValue(aFile, DavResource.PROP_RESOURCETYPE);
  }

  public void testDirectoryResourceType() throws FileSystemException {
    Element root = serializeDavResource(aDirectory, DavResource.PROP_RESOURCETYPE);
    assertNotNull(selectExistingProperty(root, DavResource.PROP_RESOURCETYPE).selectSingleNode(DavCollection.COLLECTION));
  }
}
