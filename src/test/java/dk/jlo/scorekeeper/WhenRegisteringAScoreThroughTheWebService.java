package dk.jlo.scorekeeper;

import dk.jlo.util.WSClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.performance.annotation.Performance;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.PersistenceTest;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// import org.jboss.arquillian.performance.annotation.PerformanceTest;

//@CreateSchema({"schema-creation.sql"})
@RunWith(Arquillian.class)
@PersistenceTest
@DataSource("java:/ds/postgresDS")
@UsingDataSet("tournaments.yml")
//@PerformanceTest(resultsThreshold = 1.5)
public class WhenRegisteringAScoreThroughTheWebService {

    @Deployment(testable = true, name = "ArqPersistencePluginHack", order = 0)
    @OverProtocol("Servlet 3.0") // Avoid JBAS016000
    public static EnterpriseArchive hackForPlugin() {
        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsLibrary(ShrinkWrap.create(JavaArchive.class, "test.jar")
                        .addClass(WhenRegisteringAScoreThroughTheWebService.class)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addAsResource("META-INF/persistence.xml"))
                .addAsLibrary(Maven.resolver().resolve("dk.jlo.scorekeeper:model:jar:1.0.0-SNAPSHOT")
                        .withoutTransitivity()
                        .asSingleFile());
    }

    @Deployment(testable = false, name = "scorekeeper", order = 1)
    @OverProtocol("Servlet 3.0") // Avoid JBAS016000
    public static EnterpriseArchive importEar() {
        return Maven.resolver() // get resolver instance
                .resolve("dk.jlo.scorekeeper:scorekeeper-ear:ear:1.0.0-SNAPSHOT") // get EAR from Maven repository
                .withoutTransitivity() // Don't need any of the ear's transitive dependencies
                .asSingle(EnterpriseArchive.class); // wrap the result as single object of type EnterpriseArchive
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("ArqPersistencePluginHack")
    @Cleanup(phase = TestExecutionPhase.NONE) // Avoid cleanup so that the real test can use the data.
    public void hackThePlugin() {
        System.out.println("PLUGIN HACK!");  // Notice that JBoss logging is logging the println.
        assertThat(1, is(1));
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("scorekeeper")
    @RunAsClient
    @Performance(time = 1000)
    public void theCallDoesNotFailBecauseOfUnknownTournament(@ArquillianResource URL testUrl) throws IOException {
        System.out.println("URL:" + testUrl); // Notice that System.out is logging the println.

        WSClient wsClient = WSClient.forUrl("http://" + testUrl.getHost() + ":" + testUrl.getPort()
                + "/ejb-1.0.0-SNAPSHOT/MatchWS/MatchWS")
                .usingRequestProperty("SOAPAction", "createMatch");
        // .usingRequestProperty("SOAPAction", "http://ws.scorekeeper.jlo.dk/createMatch");
        wsClient.post("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
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
                "  </soapenv:Envelope>");

        System.out.println(wsClient.getResponse());
        System.out.println("Webservice called.");
    }
}
