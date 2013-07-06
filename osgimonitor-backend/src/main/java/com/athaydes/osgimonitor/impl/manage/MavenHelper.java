package com.athaydes.osgimonitor.impl.manage;

import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Paths;

/**
 * User: Renato
 */
public class MavenHelper extends FilesHelper {

	private static final String SETTINGS_FILE_NAME = "settings.xml";

	private XmlHelper xmlHelper = new XmlHelper();

	public void setXmlHelper( XmlHelper xmlHelper ) {
		this.xmlHelper = xmlHelper;
	}

	public String getMavenHome() {
		String m2Home = getMavenHomeEnvVariable();
		if ( dirExists( m2Home ) ) {
			return m2Home;
		} else {
			String userHome = getUserHome();
			String mavenHome = userHome + File.separator + ".m2";
			if ( dirExists( mavenHome ) ) {
				return mavenHome;
			} else {
				throw new RuntimeException( "Cannot find the Maven Home" );
			}
		}
	}

	public String getMavenRepoHome() {
		File settingsFile = Paths.get( getMavenHome(), SETTINGS_FILE_NAME ).toFile();
		if ( settingsFile.exists() ) {
			try {
				Document doc = xmlHelper.parseFile( settingsFile );
				String repoLocation = xmlHelper.evalXPath( doc, "/settings/localRepository/text()" );
				if ( repoLocation != null ) {
					return repoLocation;
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return getDefaultMavenRepoHome();
	}

	protected String getMavenHomeEnvVariable() {
		return System.getenv( "M2_HOME" );
	}

	private String getDefaultMavenRepoHome() {
		return Paths.get( getMavenHome(), "repository" ).toString();
	}

}
