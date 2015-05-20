# Lightweight Transaction Testing

A simple Ratpack app for experimenting with cassandra LWTs. This is set up to do auth, client verification and ssl with the cluster.

## Config
By default it will look for a `cassandra.yml` file in the users home directory.

``` yml
cassandra:
  truststorePath: <path>
  truststorePassword: <password>
  keystorePath: <path>
  keystorePassword: <password>
  truststore:
    path:  <path>
    password: <password>
  keystore:
    path: <path>
    password: <password>  
  seeds:
    - <127.0.0.1>
    - <127.0.0.2>
  user: <username>
  password: <pass>

```

## Run

```
./gradlew run
```
