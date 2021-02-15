## Testing Goals
As a user of the Servlet Specification, we expect the container
to throw a `javax.naming.NameNotFoundException` if the required
resource (`jdbc/bad`) has not been properly configured.  That is
what we are testing for.

In this test, the servlet mounted at `/index.html` reports:

- *PASS* if getting jdbc/bad throws a NamingException is thrown as we expect.
- *FAIL* if getting jdbc/bad instead gets a BasicDataSource with no url or driverClassName property set.

As a control measure, we correctly configure `jdbc/good`
and check that it is working.  So we will get:

- *UNKNOWN* if `jdbc/good` is not also well-obtained and 'ready for action'


This test is specific to Tomcat DBCP2 and will not work on
other containers.

Tomcat 9 fails this test.


## Basic Arrangement of the Test
This webapp declares two resource references of type 
`java.sql.DataSource` in `web.xml`:

- `jdbc/good`
- `jdbc/bad`

It also supplies a configuration of ONE of these resources 
(`jdbc/good`) in `META-INF/context.xml` which Tomcat will use 
to configure it.  Since we need to give this resource 
declaration a chance at succeeding, we use the PostgreSQL
driver and a syntactically-valid corresponding JDBC URL. The
PostgreSQL driver does not attempt to use the URL until we call
`getConnection()` and since we never do that, there is no need
to configure an actual database for this test.

We deliberately DO NOT supply a configuration for `jdbc/bad`.  
This simulates the condition when no configuration for the
resource is found by Tomcat's resolution mechanism.

## Why this bug matters

The significance of the failure is that:

- the developer cannot rely on "no NamingException was thrown when I did
  the JNDI lookup" as a signal that the deployer has succeeded in configuring 
  the context.

- the deployer gets *no notice at all* (other than an uncaught NPE when
  attempting to use the webapp) that a resource on which the  webapp depends
  has not been bound to a configuration element.
  
Lacking these signals, the deploye is left guessing what part of
the configuration they 'got wrong'.  Did the JDBC driver itself 
not get loaded in the right classloader context?  Was a required
attribute of the <Resource/> element omitted?  Is the JDBC URL
correct?  Or they just blame the developer, who wrote sloppy code
that caused an NPE.

In short, a myriad of mis-directed effort and blame arise because 
it is falsely presumed that the <Context/> configuration was at 
least discovered if the JNDI lookup succeeds.