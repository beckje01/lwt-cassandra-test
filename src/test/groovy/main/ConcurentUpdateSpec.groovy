package main

import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.http.TestHttpClient
import spock.lang.Specification

class ConcurentUpdateSpec extends Specification {
	def aut = new GroovyRatpackMainApplicationUnderTest()
	@Delegate
	TestHttpClient client = TestHttpClient.testHttpClient(aut)

	def checkUpdates() {

		when:
		client.get("changePassword/333")

		and:
		def example = client.getText("user")

		then:
		example == "Found User: 333"
	}
}
