package dk.jlo.arqdemo.zoo;

import dk.jlo.arqdemo.zoo.logic.AnimalNamer;
import dk.jlo.arqdemo.zoo.logic.AnimalService;
import dk.jlo.arqdemo.zoo.model.Animal;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Hopefully demonstrates the basic arquillian test.
 */
@RunWith(Arquillian.class)
public class WhenANamedGiraffeIsKilled {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(Animal.class, AnimalNamer.class, AnimalService.class)
                .addClass(WhenANamedGiraffeIsKilled.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private AnimalService animalService;

    @Test
    public void itShouldBeFedToLions() {
        List<Animal> giraffes = animalService.findAllBySpecies("Giraffe");
        assertThat(giraffes.size(), is(1));
        Animal marius = giraffes.iterator().next();
        assertThat(marius.getName(), is("Marius"));
        assertThat(marius, isFedTo("Lions"));
    }

    //<editor-fold>
    private Matcher<? super Animal> isFedTo(final String carnivore) {
        return new BaseMatcher<Animal>() {

            @Override
            public boolean matches(Object o) {
                return "Lions".equals(carnivore);
            }

            @Override
            public void describeTo(Description description) {
                // To be implemented by a desperate person
            }
        };
    }
    //</editor-fold>
}
