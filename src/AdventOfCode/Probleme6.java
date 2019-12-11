package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Probleme6 {
    public static void main(String... args) throws IOException {
        HashMap<String, Planet> planets = new HashMap<>();
        Files.lines(Paths.get("input6.txt")).forEach(l -> {
            String[] arr = l.split("\\)");
            if (!planets.containsKey(arr[0])) planets.put(arr[0], new Planet(arr[0]));
            if (!planets.containsKey(arr[1])) planets.put(arr[1], new Planet(arr[1]));
            planets.get(arr[1]).setOrbit(planets.get(arr[0]));
            planets.get(arr[0]).addSatellite(planets.get(arr[1]));
        });
        System.out.println(planets.values().stream().mapToInt(Planet::countAllOrbitsRecursive).sum());
        planets.get("COM").addOrbitsRecursive();
        Set<Planet> temp1 = new HashSet<>(planets.get("SAN").orbits), temp2 = new HashSet<>(planets.get("YOU").orbits);
        temp1.removeAll(planets.get("YOU").orbits);
        temp2.removeAll(planets.get("SAN").orbits);
        System.out.println(temp1.size() + temp2.size());
    }
}

class Planet {
    private Planet orbit;
    private List<Planet> satellites;
    Set<Planet> orbits;
    private int numberOfOrbits = -1;
    private String name;

    Planet(String name) {
        this.name = name;
        satellites = new ArrayList<>();
        orbits = new HashSet<>();
    }

    void setOrbit(Planet planet) {
        orbit = planet;
    }

    private void addOrbit(Planet p) {
        orbits.add(p);
        orbits.addAll(p.orbits);
    }

    void addSatellite(Planet p) {
        satellites.add(p);
    }

    @Override
    public String toString() {
        return name;
    }

    int countAllOrbitsRecursive() {
        return numberOfOrbits = (numberOfOrbits == -1 ? (orbit == null ? 0 : 1 + orbit.countAllOrbitsRecursive()) : numberOfOrbits);
    }

    void addOrbitsRecursive() {
        satellites.forEach(s -> {
            s.addOrbit(this);
            s.addOrbitsRecursive();
        });
    }
}
