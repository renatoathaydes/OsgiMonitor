package com.athaydes.osgimonitor.impl.manage.osgi;

import aQute.bnd.make.MakeBnd;

import java.io.File;
import java.util.jar.JarFile;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * User: Renato
 */
public class OsgiHelper {

	private URLStreamHandlerService paxService;

	public void wrapJarIntoBundle( JarFile jar, File destination ) {
		//http://stackoverflow.com/questions/4673406/programatically-start-osgi-equinox
		//BundleContext bc = fwk.getBundleContext();
		//bc.installBundle("file:/path/to/bundle.jar");
		//Bundle bundle = context.installBundle(
		//		"http://www.eclipsezone.com/files/jsig/bundles/HelloWorld.jar");
		//bc.registerService(MyService.class.getName(), new MyServiceImpl(), null);
	}

}
