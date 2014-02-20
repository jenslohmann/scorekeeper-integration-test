package dk.jlo.arqdemo.poc;

import dk.jlo.scorekeeper.model.Tournament;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.PersistenceTest;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Demonstrates basic use of the Persistence Extension stuff.
 */
//@CreateSchema({"schema-creation.sql"})
@RunWith(Arquillian.class)
@PersistenceTest
@DataSource("jdbc/scorekeeperDS")
@UsingDataSet("tournaments.yml")
public class UsingPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Deployment(testable = true, name = "ArqPersistencePluginHack", order = 0)
    @OverProtocol("Servlet 3.0") // Avoid JBAS016000
    public static EnterpriseArchive hackForPlugin() {
        return ShrinkWrap.create(EnterpriseArchive.class, "PersistenceTest.ear")
                .addAsLibrary(ShrinkWrap.create(JavaArchive.class, "test.jar")
                        .addClass(UsingPersistenceTest.class)
                                // Avoid BeanManager not found (enables CDI)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addAsResource("META-INF/persistence.xml"))
                .addAsLibrary(Maven.resolver().resolve("dk.jlo.scorekeeper:model:jar:1.0.0-SNAPSHOT").withoutTransitivity()
                        .asSingleFile());
    }

    @Test
    @InSequence(1)
    @UsingDataSet("tournaments.yml")
    @OperateOnDeployment("ArqPersistencePluginHack")
    public void hackThePlugin() {
        System.out.println("PLUGIN HACK!");

        List<Tournament> result = em.createQuery("select t from Tournament t").getResultList();

        assertThat(1, is(1));
    }
}
