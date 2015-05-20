package service

/**
 * Created by beckje01 on 5/20/15.
 */
class CassandraConfig {

	JKSConfig truststore
	JKSConfig keystore

	String user
	String password

	List<String> seeds

	class JKSConfig {
		String path
		String password
	}

}

