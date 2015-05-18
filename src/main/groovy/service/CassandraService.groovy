package service

import com.datastax.driver.core.AuthProvider
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.SSLOptions
import com.datastax.driver.core.Session
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.security.SecureRandom

class CassandraService extends AbstractModule {

	Cluster cluster
	Session session

	String[] cipherSuites = ["TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"];
	String truststorePath = "/Users/beckje01/scratch/dev-cass/.dev-truststore"
	String truststorePassword = "Password1"
	String keystorePath = "/Users/beckje01/scratch/dev-cass/.dev-truststore"
	String keystorePassword = "Password1"

	@Override
	protected void configure() {

		SSLContext sslContext = getSSLContext(truststorePath, truststorePassword, keystorePath, keystorePassword);

		cluster = Cluster.builder().addContactPoint("54.84.83.91")
			.withSSL(new SSLOptions(sslContext, cipherSuites))
			.withCredentials("client", "")
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
