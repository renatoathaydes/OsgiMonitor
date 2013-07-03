package com.athaydes.osgimonitor.impl.manage;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 * User: Renato
 */
public class XmlHelper {

	private DocumentBuilder docBuilder;
	private final XPath xPath = XPathFactory.newInstance().newXPath();

	public Document parseFile( File xmlFile )
			throws IOException, SAXException {
		DocumentBuilder builder = getDocBuilder();
		if ( builder != null )
			return builder.parse( xmlFile );
		return null;
	}

	public String evalXPath( Document doc, String expression ) {
		try {
			return xPath.compile( expression ).evaluate( doc );
		} catch ( XPathExpressionException e ) {
			e.printStackTrace();
		}
		return null;
	}

	private DocumentBuilder getDocBuilder() {
		if ( docBuilder == null ) {
			docBuilder = createDocBuilder();
		}
		return docBuilder;
	}

	private static DocumentBuilder createDocBuilder() {
		DocumentBuilderFactory builderFactory =
				DocumentBuilderFactory.newInstance();
		try {
			return builderFactory.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		}
		return null;
	}

}
