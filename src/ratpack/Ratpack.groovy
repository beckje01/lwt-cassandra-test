import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.Requests
import com.datastax.driver.core.Session
import com.datastax.driver.core.Statement
import com.datastax.driver.core.querybuilder.Clause
import com.datastax.driver.core.querybuilder.QueryBuilder
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
		get("create") {
			def session = context.get(Session)

			//Read user first cause
			Statement stmt = QueryBuilder.select().all().from("global", "user").where(QueryBuilder.eq("username", "beckje01")).setConsistencyLevel(ConsistencyLevel.SERIAL)
			def rs = session.execute(stmt)

			def existingUser = rs.one()
			if (existingUser) {
				render "Found User: " + existingUser.getString("password")
			} else {
				Statement insert = QueryBuilder.insertInto("global", "user").value("password", "passHash").value("username", "beckje01").ifNotExists().setConsistencyLevel(ConsistencyLevel.QUORUM)
				def result = session.execute(insert)
				render result.one().getColumnDefinitions().asList()*.name.toString()
			}
		}

		get("user") {
			def session = context.get(Session)
			Statement stmt = QueryBuilder.select().all().from("global", "user").where(QueryBuilder.eq("username", "beckje01")).setConsistencyLevel(ConsistencyLevel.ONE)

			def rs = session.execute(stmt)
			def existingUser = rs.one()
			render "Found User: " + existingUser.getString("password")
		}

		get("userQ") {
			def session = context.get(Session)
			Statement stmt = QueryBuilder.select().all().from("global", "user").where(QueryBuilder.eq("username", "beckje01")).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)

			def rs = session.execute(stmt)
			def existingUser = rs.one()
			render "Found User: " + existingUser.getString("password")
		}

		get("changePassword/:password") {
			def session = context.get(Session)

			String pwd = context.pathTokens.get("password")
			//Read user first cause
			Statement stmt = QueryBuilder.select().all().from("global", "user").where(QueryBuilder.eq("username", "beckje01")).setConsistencyLevel(ConsistencyLevel.SERIAL)
			def rs = session.execute(stmt)

			def existingUser = rs.one()
			if (existingUser) {

				def oldPwd = existingUser.getString("password")

				Statement update = QueryBuilder.update("global", "user").with(QueryBuilder.set("password", pwd)).where(QueryBuilder.eq("username", "beckje01")).onlyIf(QueryBuilder.eq("password", oldPwd))
				def updateResult = session.execute(update)

				render "" + updateResult.one().getBool("[applied]")

			} else {
				render "User Not found"
			}
		}

	}
}
