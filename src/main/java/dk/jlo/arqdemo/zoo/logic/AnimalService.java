package dk.jlo.arqdemo.zoo.logic;

import dk.jlo.arqdemo.zoo.model.Animal;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AnimalService {

    //    @PersistenceContext
//    private EntityManager em;

    @Inject
    private AnimalNamer animalNamer;

    public List<Animal> findAllBySpecies(String species) {
        // Test first, code later. Assume Giraffe!
        ArrayList<Animal> animals = new ArrayList<Animal>(1);
        Animal animal = new Animal();
        animal.setName(animalNamer.produceNameFor(species));
        animal.setSpecies(species);
        animals.add(animal);
        return animals;
    }
}
