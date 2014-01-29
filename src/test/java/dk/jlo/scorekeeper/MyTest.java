package dk.jlo.scorekeeper;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.*;
import org.jboss.arquillian.test.api.ArquillianResource;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

//@CreateSchema({"schema-creation.sql"})
@RunWith(Arquillian.class)
@PersistenceTest
@DataSource("java:/ds/postgresDS")
@UsingDataSet("tournaments.yml")
public class MyTest {

    @Deployment(testable = true, name = "ArqPersistencePluginHack", order = 0)
    @OverProtocol("Servlet 3.0") // Avoid JBAS016000
    public static EnterpriseArchive hackForPlugin() {
        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsLibrary(ShrinkWrap.create(JavaArchive.class, "test.jar")
                        .addClass(MyTest.class)
                                // Avoid BeanManager not found (enables CDI)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addAsResource("META-INF/persistence.xml"))
                .addAsLibrary(Maven.resolver().resolve("dk.jlo.scorekeeper:model:jar:1.0.0-SNAPSHOT").withoutTransitivity()
                .asSingleFile());
    }

    @Deployment(testable = false, name = "scorekeeper", order = 1)
    @OverProtocol("Servlet 3.0") // Avoid JBAS016000
    public static EnterpriseArchive importEar() {
        return Maven.resolver() // get resolver instance
                .resolve("dk.jlo.scorekeeper:scorekeeper-ear:ear:1.0.0-SNAPSHOT") // get EAR from Maven repository - note you might want to set your own Maven repository first via document above
                .withoutTransitivity() // Don't need any ear's transitive dependencies
                .asSingle(EnterpriseArchive.class); // wrap the result as single object of type EnterpriseArchive
    }

    @Test
    @InSequence(1)
//    @UsingDataSet("tournaments.yml")
    @OperateOnDeployment("ArqPersistencePluginHack")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public void hackThePlugin() {
        System.out.println("PLUGIN HACK!");
        assertThat(1, is(1));
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("scorekeeper")
    @RunAsClient
    public void test(@ArquillianResource URL testUrl) throws IOException {
        System.out.println("URL:" + testUrl);
        URL url = new URL(testUrl.getProtocol() + "://" + testUrl.getHost() + ":" + testUrl.getPort()
                + "/ejb-1.0.0-SNAPSHOT/MatchWS/MatchWS");
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
