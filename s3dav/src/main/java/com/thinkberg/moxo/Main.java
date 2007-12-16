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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * The launcher is responsible for extracting referenced libraries from the jar
 * and setting up the classpath.
 * 
 * @author Matthias L. Jugel
 */
public class Main {
	private final static URL location = Main.class.getProtectionDomain()
			.getCodeSource().getLocation();

	@SuppressWarnings( { "RedundantArrayCreation" })
	public static void main(String args[]) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		ClassLoader parentClassLoader = Thread.currentThread()
				.getContextClassLoader();
		if (null == parentClassLoader) {
			parentClassLoader = Main.class.getClassLoader();
		}
		if (null == parentClassLoader) {
			parentClassLoader = ClassLoader.getSystemClassLoader();
		}
		URLClassLoader classLoader = new URLClassLoader(initClassPath(),
				parentClassLoader);
		Thread.currentThread().setContextClassLoader(classLoader);

		System.setSecurityManager(null);

		try {
			Policy.getPolicy().refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Class mainClass = classLoader
				.loadClass("com.thinkberg.moxo.MoxoJettyRunner");
		final Method main = mainClass.getDeclaredMethod("main",
				new Class[] { String[].class });
		main.invoke(null, new Object[] { args });
	}

	/**
	 * Read the jar manifest and add class-path entries to the classpath.
	 * 
	 * @return the classpath
	 */
	private static URL[] initClassPath() {
		List<URL> urlArray = new ArrayList<URL>();
		InputStream manifestIn = null;
		try {
			manifestIn = location.openStream();
			JarInputStream launcherJarIs = new JarInputStream(manifestIn);
			StringBuffer classPath = new StringBuffer(location.getFile());
			List<URL> classpathList = new ArrayList<URL>(urlArray);
			JarEntry jarEntry;
			while (null != (jarEntry = launcherJarIs.getNextJarEntry())) {
				if (!jarEntry.isDirectory()
						&& jarEntry.getName().endsWith(".jar")) {
					try {
						URL classPathEntry = getResourceUrl(jarEntry.getName());
						if (!classpathList.contains(classPathEntry)) {
							urlArray.add(classPathEntry);
							classPath.append(File.pathSeparatorChar);
							classPath.append(classPathEntry.getFile());
						}
					} catch (IOException e) {
						System.err.println("ignored '" + jarEntry.getName()
								+ "'");
					}
				}
			}

			System.setProperty("java.class.path", classPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (manifestIn != null) {
				try {
					manifestIn.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return urlArray.toArray(new URL[0]);
	}

	/**
	 * Get URL.
	 * 
	 * @param resource
	 *            resource name/path
	 * @return the url pointing to the resource
	 * @throws IOException
	 *             if the resource cannot be accessed
	 */
	private static URL getResourceUrl(String resource) throws IOException {
		File directoryBase = new File(location.getFile()).getParentFile();
		File file = new File(resource);
		if (file.isAbsolute() && file.exists()) {
			return file.toURL();
		}
		file = new File(directoryBase, resource);
		if (file.exists()) {
			return file.toURL();
		}

		URL resourceURL = Main.class.getResource("/" + resource);
		if (null != resourceURL) {
			return extract(resourceURL);
		}

		throw new MalformedURLException(resource);
	}

	/**
	 * Extract file from launcher jar to be able to access is via classpath.
	 * 
	 * @param resource
	 *            the jar resource to be extracted
	 * @return a url pointing to the new file
	 * @throws IOException
	 *             if the extraction was not possible
	 */
	private static URL extract(URL resource) throws IOException {
		File f = File.createTempFile("launcher_", ".jar");
		f.deleteOnExit();
		if (f.getParentFile() != null) {
			f.getParentFile().mkdirs();
		}
		InputStream is = new BufferedInputStream(resource.openStream());
		FileOutputStream os = new FileOutputStream(f);
		byte[] arr = new byte[8192];
		for (int i = 0; i >= 0; i = is.read(arr)) {
			os.write(arr, 0, i);
		}
		is.close();
		os.close();
		return f.toURL();
	}
}