package com.thinkberg.moxo;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.jets3t.service.Jets3tProperties;

import com.thinkberg.moxo.dav.ResourceManager;

/**
 * The resource manager is responsible for providing a virtual file system root.
 * 
 * @author Matthias L. Jugel
 * @author Stian Soiland
 */
public class S3ResourceManager extends ResourceManager {
	private FileObject root;

	public S3ResourceManager() {
		try {
			String propertiesFileName = System.getProperty("moxo.properties",
					"moxo.properties");
			Jets3tProperties properties = Jets3tProperties
					.getInstance(propertiesFileName);
			FileSystemManager fsm = VFS.getManager();
			FileObject rootObject = fsm.resolveFile("s3://"
					+ properties.getStringProperty("bucket", null));
			root = fsm.createVirtualFileSystem(rootObject);
			System.err.println("Created virtual file system: " + rootObject);
		} catch (FileSystemException e) {
			System.err.println("Can't create virtual file system: "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public FileObject getFileObject(String path) throws FileSystemException {
		return root.resolveFile(path);
	}

}
