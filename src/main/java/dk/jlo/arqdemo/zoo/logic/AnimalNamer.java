package dk.jlo.arqdemo.zoo.logic;

public class AnimalNamer {
    public String produceNameFor(String species) {
        if (species.equals("Giraffe")) {
            return "Marius";
        } else {
            return "Animal no. 42";
        }
    }
}
