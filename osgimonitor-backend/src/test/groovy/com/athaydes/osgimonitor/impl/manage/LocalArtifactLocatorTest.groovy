package com.athaydes.osgimonitor.impl.manage

import com.athaydes.osgimonitor.api.manage.ArtifactLocator
import spock.lang.Specification

/**
 *
 * User: Renato
 */
class LocalArtifactLocatorTest extends Specification {

	def "A single dependency can be fetched by entering a groupId and artifactId"( ) {
		given:
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

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
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

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
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

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
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

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
