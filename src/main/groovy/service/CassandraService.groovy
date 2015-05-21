package service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PerHostPercentileTracker
import com.datastax.driver.core.SSLOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy
import com.datastax.driver.core.policies.PercentileSpeculativeExecutionPolicy
import com.datastax.driver.core.policies.TokenAwarePolicy
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import ratpack.config.ConfigData

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.security.SecureRandom

class CassandraService extends AbstractModule {

	Cluster cluster
	Session session

	ConfigData configData

	String[] cipherSuites = ["TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"];

	public CassandraService(ConfigData configData) {
		this.configData = configData
	}

	@Override
	protected void configure() {

		def cassandraConfig = configData.get("/cassandra", CassandraConfig)

		SSLContext sslContext = getSSLContext(cassandraConfig.truststore.path, cassandraConfig.truststore.password, cassandraConfig.keystore.path, cassandraConfig.keystore.password);

		PerHostPercentileTracker tracker = PerHostPercentileTracker
			.builderWithHighestTrackableLatencyMillis(5000)
			.build();

		//Groovy so just use the private constructor
		def builder = Cluster.builder().withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy(null, 1, false, true)))
			.withSpeculativeExecutionPolicy(new PercentileSpeculativeExecutionPolicy(tracker, 0.99, 3))

		for (String seed : cassandraConfig.seeds) {
			builder.addContactPoint(seed)
		}

		cluster = builder.withSSL(new SSLOptions(sslContext, cipherSuites))
			.withCredentials(cassandraConfig.user, cassandraConfig.password)
			.build()

		session = cluster.connect()
	}

	@Provides
	@Singleton
	Session providesSession() {
		return this.session
	}

	private
	static SSLContext getSSLContext(String truststorePath, String truststorePassword, String keystorePath, String keystorePassword) throws Exception {
		FileInputStream tsf = new FileInputStream(truststorePath);
		FileInputStream ksf = new FileInputStream(keystorePath);
		SSLContext ctx = SSLContext.getInstance("SSL");

		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(tsf, truststorePassword.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);

		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(ksf, keystorePassword.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

		kmf.init(ks, keystorePassword.toCharArray());

		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		return ctx;
	}

}
