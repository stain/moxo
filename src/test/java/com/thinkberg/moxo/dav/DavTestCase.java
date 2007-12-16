package com.thinkberg.moxo.dav;

import com.thinkberg.moxo.dav.data.DavResource;
import com.thinkberg.moxo.dav.data.DavResourceFactory;
import junit.framework.TestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Arrays;

/**
 * Helper class for DAV tests.
 *
 * @author Matthias L. Jugel
 */
public class DavTestCase extends TestCase {
  private static final String PROP_EXISTS = "propstat[status='HTTP/1.1 200 OK']/prop/";
  private static final String PROP_MISSING = "propstat[status='HTTP/1.1 404 Not Found']/prop/";
  private static final String EMPTY = "";

  FileObject aFile;
  FileObject aDirectory;


  protected void setUp() throws Exception {
    super.setUp();
    FileSystemManager fsm = VFS.getManager();
    FileObject fsRoot = fsm.createVirtualFileSystem(fsm.resolveFile("ram:/"));
    aFile = fsRoot.resolveFile("/file.txt");
    aFile.delete();
    aFile.createFile();
    aDirectory = fsRoot.resolveFile("/folder");
    aDirectory.delete();
    aDirectory.createFolder();
  }

  @SuppressWarnings({"SameParameterValue"})
  protected void testPropertyValue(FileObject object, String propertyName, String propertyValue) throws FileSystemException {
    Element root = serializeDavResource(object, propertyName);
    assertEquals(propertyValue, selectExistingPropertyValue(root, propertyName));
  }

  protected void testPropertyNoValue(FileObject object, String propertyName) throws FileSystemException {
    Element root = serializeDavResource(object, propertyName, true);
    assertEquals(EMPTY, selectExistingPropertyValue(root, propertyName));
  }

  Node selectExistingProperty(Element root, String propertyName) {
    return root.selectSingleNode(PROP_EXISTS + propertyName);
  }

  Node selectMissingProperty(Element root, String propertyName) {
    return root.selectSingleNode(PROP_MISSING + propertyName);
  }

  @SuppressWarnings({"SameParameterValue"})
  String selectMissingPropertyName(Element root, String propertyName) {
    return selectMissingProperty(root, propertyName).getName();
  }

  private String selectExistingPropertyValue(Element root, String propertyName) {
    return selectExistingProperty(root, propertyName).getText();
  }

  Element serializeDavResource(FileObject object, String propertyName) throws FileSystemException {
    return serializeDavResource(object, propertyName, false);
  }

  private Element serializeDavResource(FileObject object, String propertyName, boolean ignoreValues) throws FileSystemException {
    Element root = DocumentHelper.createElement("root");
    DavResourceFactory factory = DavResourceFactory.getInstance();
    DavResource davResource = factory.getDavResource(object);
    davResource.setIgnoreValues(ignoreValues);
    davResource.serializeToXml(root, Arrays.asList(propertyName));
    return root;
  }

}
