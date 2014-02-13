package dk.jlo.arqdemo.zoo.logic;

import dk.jlo.arqdemo.zoo.model.Animal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

public class AnimalService {

    @PersistenceContext
    private EntityManager em;

    public List<Animal> findAllBySpecies(String species) {
        // Test first, code later. Assume Giraffe!
        ArrayList<Animal> animals = new ArrayList<Animal>(1);
        Animal marius = new Animal();
        marius.setName("Marius");
        marius.setSpecies("Giraffe");
        animals.add(marius);
        return animals;
    }
}
