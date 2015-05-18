import com.datastax.driver.core.Session
import service.CassandraService

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
	bindings {
		add CassandraService
	}

	handlers {
		get {
			def session = context.get(Session)



			render session.getCluster().getClusterName()
		}

	}
}
