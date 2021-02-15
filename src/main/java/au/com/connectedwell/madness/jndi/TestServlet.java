package au.com.connectedwell.madness.jndi;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_OK;

public class
TestServlet extends HttpServlet {
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        BasicDataSource good;
        BasicDataSource bad = null;
        boolean namingExceptionThrown = false;

        try {

            var jndi = new InitialContext();
            good = (BasicDataSource) jndi.lookup("java:comp/env/jdbc/good");

            try {
                bad = (BasicDataSource) jndi.lookup("java:comp/env/jdbc/bad");
            } catch (NamingException e) {
                // I wish
                namingExceptionThrown = true;
            }

        } catch (NamingException e) {
            // won't happen under normal test
            throw new ServletException(
                    "We got a NamingException this test didn't expect.  "
                            + "Something is not right with the test setup",
                    e
            );

        }

        boolean haveGood = good != null && good.getUrl() != null && good.getDriverClassName() != null;
        boolean haveBad = bad != null && bad.getUrl() == null && bad.getDriverClassName() == null;

        String result;
        if (haveGood & !haveBad && namingExceptionThrown) {
            result = "PASS.  Got resource 'jdbc/good'.  NamingException thrown for 'jdbc/bad' which we know is not configured.";
        } else if (haveGood && haveBad) {
            result = "FAIL.  Got resource 'jdbc/good'.  But also got resource for 'jdbc/bad', which we know is not configured.  It has url=null and driver=null and will fail an attempt to #getConnection()";
        } else {
            result = "UNKNOWN.  This test failed to test what it thought it was testing.";
        }

        result += "\nServlet container is: " + request.getServletContext().getServerInfo();

        response.setStatus(SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.setContentLength(result.length());

        PrintWriter w = response.getWriter();
        w.print(result);
        w.close();

    }

}