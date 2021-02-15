### Testing Goals
As a user of the Servlet Specification, we expect the container
to throw a `javax.naming.NameNotFoundException` if a required
resource has not been properly configured.

In this test, the servlet mounted at `/index.html` reports:

- *PASS* if getting `jdbc/bad` throws a `NamingException` as we expect it should
- *FAIL* if getting `jdbc/bad` gets a `BasicDataSource` with no `url` or `driverClassName` property set

As a control measure, we correctly configure `jdbc/good`.  So we will get:

- *UNKNOWN* if `jdbc/good` is not also well-obtained and 'ready for action'
  ... this would indicate that some our assumptions for the test are not in 
  fact valid.


### Tomcat only
This test is specific to Tomcat DBCP2 and will not work on
other containers.

Because it is Tomcat which fails this test.


### How to run: use of Gradle and Gretty

Check out the project and invoke the `appRun` gradle task
provided by the `gretty` plugin.  Browse to the URL to see
the results of the test.


### Basic Arrangement of the Test
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

### Why this bug matters

The significance of the failure is that:

- the developer cannot rely on "no NamingException was thrown when I did
  the JNDI lookup" as a signal that the deployer has succeeded in configuring 
  the context.

- the deployer gets *no notice at all* (other than an uncaught NPE when
  attempting to use the webapp) that a resource on which the  webapp depends
  has not been bound to a configuration element.
  
Lacking these signals, the hapless deployer is left guessing what part of
the configuration they 'got wrong'.  Did the JDBC driver itself 
not get loaded in the right classloader context?  Was a required
attribute of the <Resource/> element omitted?  Is the JDBC URL
correct?  Or they just blame the developer, who wrote sloppy code
that caused an NPE.

In short, a myriad of mis-directed effort and blame arise because 
it is falsely presumed that the <Context/> configuration was at 
least discovered if the JNDI lookup succeeds.

A Google search for [tomcat java.sql.SQLException: Cannot create JDBC driver of class '' for connect URL 'null'](https://www.google.com/search?q=tomcat+java.sql.SQLException%3A+Cannot+create+JDBC+driver+of+class+%27%27+for+connect+URL+%27null%27) reveals the
worldwide carnage.  The 'solution' most-given is only partially correct:

> put it in `META-INF/context.xml` ... it works though I dunno why'

which really does little to promote understanding of the otherwise-good
options for configuring Tomcat contexts, which are available.

### Bottom Line
Really, the deployer is due a message like: 

    hey, er, the webapp declared some resources,
    but you didn't supply a configuration for them
    ... that I found, anyway.  You need to configure 
    [list of missing resources].

and in Tomcat, they don't get one.