package com.thinkberg.moxo.s3dav.servlet;

import com.thinkberg.moxo.S3ResourceManager;
import com.thinkberg.moxo.dav.ResourceManager;
import com.thinkberg.moxo.servlet.MoxoWebdavServlet;

public class MoxoS3WebdavServlet extends MoxoWebdavServlet {

	private ResourceManager resourceManager = new S3ResourceManager();

	@Override
	public ResourceManager getResourceManager() {
		return resourceManager;
	}

}
