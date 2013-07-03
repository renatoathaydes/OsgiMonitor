package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class XmlHelperTest extends Specification {

	def "A XML file can be parsed into a Document"( ) {
		given:
		"A XML file with known contents"
		def targetDir = Paths.get( 'target', this.class.simpleName ).toFile()
		targetDir.mkdirs()
		def xmlFile = new File( targetDir, 'f.xml' ) <<
				"""
				|<root>
				|	<t1></t1>
				|	<t2>
				|		<t2-1/>
				|		<t2-2/>
				|	</t2>
				|</root>
				|""".stripMargin()

		when:
		"I parse it"
		def result = new XmlHelper().parseFile( xmlFile )

		then:
		"I get a Document which represents the XML file contents"
		def root = result.getElementsByTagName( 'root' )
		root.length == 1
		def rootT1 = root.item( 0 ).childNodes.item( 1 )
		rootT1.nodeName == 't1'
		def rootT2 = root.item( 0 ).childNodes.item( 3 )
		rootT2.nodeName == 't2'
		rootT1.childNodes.length == 0
		rootT2.childNodes.length == 5
		rootT2.childNodes.item( 1 ).nodeName == 't2-1'
		rootT2.childNodes.item( 3 ).nodeName == 't2-2'
		rootT2.childNodes.item( 1 ).childNodes.length == 0
		rootT2.childNodes.item( 3 ).childNodes.length == 0

		cleanup:
		safeDelete targetDir

	}

	def "A XPath expression can be used to find contents in XML files"( ) {
		given:
		"A XML file with known contents"
		def targetDir = Paths.get( 'target', this.class.simpleName ).toFile()
		targetDir.mkdirs()
		def xmlFile = new File( targetDir, 'f.xml' ) <<
				"""
				|<root>
				|	<t1>Contents of T1</t1>
				|	<t2>
				|		<t2-1 id="id-t2-1">This is T2-1</t2-1>
				|		<t2-2/>
				|	</t2>
				|</root>
				|""".stripMargin()

		when:
		"I parse it"
		def helper = new XmlHelper()
		def doc = helper.parseFile( xmlFile )

		and:
		"Ask for the contents using XPath expressions"
		def result = helper.evalXPath( doc, xPathExpression )

		then:
		"I get the expected values for each expression"
		result == expected

		cleanup:
		safeDelete targetDir

		where:
		xPathExpression                       | expected
		"/root/t1/text()"                     | 'Contents of T1'
		"/root/t2/t2-1[@id='id-t2-1']/text()" | 'This is T2-1'


	}

}
