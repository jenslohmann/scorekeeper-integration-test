package dk.jlo.scorekeeper;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(Arquillian.class)
public class MyTest {

    @Deployment(testable = false, name = "scorekeeper", order = 1)
    public static EnterpriseArchive importEar() {
        return Maven.resolver() // get resolver instance
                .resolve("dk.jlo.scorekeeper:scorekeeper-ear:ear:1.0.0-SNAPSHOT") // get EAR from Maven repository - note you might want to set your own Maven repository first via document above
                .withoutTransitivity() // you don't need any ear's transitive deps
                .asSingle(EnterpriseArchive.class); // wrap the result as single object of type EnterpriseArchive
    }

    @Deployment(order = 1000)
    public static JavaArchive deployTestStuff() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws IOException {
        URL url = new URL("http://localhost:8080/ejb-1.0.0-SNAPSHOT/MatchWS/MatchWS");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        String xmlInput = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:sc=\"http://ws.scorekeeper.jlo.dk/\">" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <sc:createMatch>\n" +
                "        <tournament>A</tournament>\n" +
                "          <team1>A</team1>\n" +
                "          <team2>B</team2>\n" +
                "          <score1>1</score1>\n" +
                "          <score2>2</score2>\n" +
                "      </sc:createMatch>\n" +
                "   </soapenv:Body>\n" +
                "  </soapenv:Envelope>";

        bout.write(xmlInput.getBytes());
        byte[] b = bout.toByteArray();
        String SOAPAction = "http://ws.scorekeeper.jlo.dk/createMatch";
        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction", SOAPAction);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        OutputStream out = httpConn.getOutputStream();
        //Write the content of the request to the outputstream of the HTTP Connection.
        out.write(b);
        out.close();
        //Ready with sending the request.

        //Read the response.
        InputStreamReader isr =
                new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        //Write the SOAP message response to a String.
        String outputString = "";
        String responseString;
        while ((responseString = in.readLine()) != null) {
            outputString = outputString + responseString;
        }
        System.out.println(outputString);
        System.out.println("Webservice called.");
    }
}
