import com.datastax.driver.core.Session
import ratpack.config.ConfigData
import ratpack.func.Action
import ratpack.server.ServerConfig
import service.CassandraService

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
	bindings {
		ConfigData configData = ConfigData.of { d ->
			d.onError(Action.noop()).yaml(System.getProperty("user.home") + "/cassandra.yml")
			d.env()
			d.sysProps()
		}

		bindInstance ConfigData, configData
		add new CassandraService(configData)
	}

	handlers {
		get {
			def session = context.get(Session)



			render session.getCluster().getClusterName()
		}

	}
}
