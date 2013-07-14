package com.athaydes.osgimonitor.impl.manage

import com.athaydes.osgimonitor.api.manage.VersionedArtifact
import spock.lang.Specification

/**
 *
 * User: Renato
 */
class RemoteArtifactLocatorTest extends Specification {

	private static final String SAMPLE_MAVEN_RESPONSE = '''<?xml version="1.0" encoding="UTF-8"?>
		<response>
			<lst name="responseHeader">
				<int name="status">0</int>
				<int name="QTime">1</int>
				<lst name="params">
					<str name="spellcheck">true</str>
					<str name="fl">id,g,a,latestVersion,p,ec,repositoryId,text,timestamp,versionCount</str>
					<str name="sort">score desc,timestamp desc,g asc,a asc</str>
					<str name="indent">off</str>
					<str name="q">g:junit AND a:junit</str>
					<str name="spellcheck.count">5</str>
					<str name="wt">xml</str>
					<str name="rows">5</str>
					<str name="version">2.2</str>
				</lst>
			</lst>
			<result name="response" numFound="1" start="0">
				<doc>
					<str name="a">junit</str>
					<arr name="ec">
						<str>-sources.jar</str>
						<str>-javadoc.jar</str>
						<str>.jar</str>
						<str>.pom</str>
					</arr>
					<str name="g">org.junit</str>
					<str name="id">org.junit:junit</str>
					<str name="latestVersion">4.11</str>
					<str name="p">jar</str>
					<str name="repositoryId">central</str>
					<arr name="text">
						<str>junit</str>
						<str>junit</str>
						<str>-sources.jar</str>
						<str>-javadoc.jar</str>
						<str>.jar</str>
						<str>.pom</str>
					</arr>
					<long name="timestamp">1352920907000</long>
					<int name="versionCount">20</int>
				</doc>
			</result>
			<lst name="spellcheck">
				<lst name="suggestions"/>
			</lst>
		</response>
		'''

	def "Maven Repository XML responses can be parsed correctly"( ) {
		given:
		"A Maven Repository XML response"
		def sampleXml = SAMPLE_MAVEN_RESPONSE

		when:
		"The response is parsed"
		List<VersionedArtifact> artifacts = new RemoteArtifactLocator()
				.parseMavenXmlResponse( sampleXml );

		then:
		"All information from the response is correctly understood"
		artifacts != null
		artifacts.size() == 1
		artifacts[ 0 ].groupId == 'org.junit'
		artifacts[ 0 ].artifactId == 'junit'
		artifacts[ 0 ].version == '4.11'
	}

	def "A single dependency can be fetched by entering a groupId and artifactId"( ) {
		given:
		"A RemoteArtifactLocator"
		def locator = new RemoteArtifactLocator()

		when:
		"An exact match is requested for a groupId and artifactId"
		def artifact = locator.findArtifact( groupId, artifactId )

		then:
		"The artifact is correctly located"
		artifact != null
		artifact.groupId == groupId
		artifact.artifactId == artifactId

		where:
		groupId               | artifactId
		"junit"               | "junit"
		"org.codehaus.groovy" | "groovy-all"

	}

	def "A set of artifacts can be found by entering a class name"( ) {
		given:
		"A RemoteArtifactLocator"
		def locator = new RemoteArtifactLocator()

		when:
		"A search by class name is made"
		def artifacts = locator.findByClassName( className )

		then:
		"A number of artifacts are found"
		artifacts != null
		!artifacts.isEmpty()
		artifacts.collect { "${it.artifactId}:${it.groupId}" }.unique().size() == artifacts.size()

		where:
		className << [ "StringUtils", "HttpResponse" ]
	}

	def "A set of artifacts can be found by entering a groupId"( ) {
		given:
		"A RemoteArtifactLocator"
		def locator = new RemoteArtifactLocator()

		when:
		"A search by groupId is made"
		def artifacts = locator.findByGroupId( groupId )

		then:
		"A number of artifacts are found"
		artifacts != null
		!artifacts.isEmpty()
		artifacts.each { assert it.groupId == groupId }
		artifacts.collect { it.artifactId }.unique().size() == artifacts.size()

		where:
		groupId << [ "com.google.inject", "junit" ]
	}

	def "A set of artifacts can be found by entering an artifactId"( ) {
		given:
		"A RemoteArtifactLocator"
		def locator = new RemoteArtifactLocator()

		when:
		"A search by artifactId is made"
		def artifacts = locator.findByArtifactId( artifactId )

		then:
		"A number of artifacts are found (note that uniqueness requires also groupId)"
		artifacts != null
		!artifacts.isEmpty()
		//println artifacts.collect { "${it.groupId}:${it.artifactId}" }
		artifacts.each { assert it.artifactId == artifactId }
		artifacts.collect { it.groupId }.unique().size() == artifacts.size()

		where:
		artifactId << [ "guice", "junit" ]
	}
}
