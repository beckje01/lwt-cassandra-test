import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
	bindings {
	}

	handlers {
		get {
		 render "test"
		}

	}
}
