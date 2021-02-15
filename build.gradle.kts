repositories {
    mavenCentral()
    jcenter()  // gretty currently needs this
}

plugins {
    java
    war
    id("org.gretty") version ("3.0.3")
}

group = "org.example"
version = "1.0-SNAPSHOT"


dependencies {

    // the 'gretty' scope is equivalent to $CATALINA_HOME/lib
    // and it doesn't put entries into the compile/implementation scope
    // for us to compile against (we specifically reference
    // org.apache.tomcat.dbcp.dbcp2.BasicDataSource in this code) ...
    gretty("org.apache.tomcat:tomcat-dbcp:9.0.43")

    // ... so we import it again, here
    compileOnly("org.apache.tomcat:tomcat-dbcp:9.0.43")


    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    // we use PostgreSQL for the test, but any driver will do, so long
    // as it doesn't attempt to verify the host/database until #getConnection()
    // is called.  This test doesn't actually call #getConnection() ...
    // it is only testing what is put into the 'java:/comp/env' JNDI
    // namspace by Tomcat.
    runtimeOnly("org.postgresql:postgresql:42.2.18")
}

gretty {
    httpPort = 18099
    servletContainer = "tomcat9"
    enableNaming = true
}