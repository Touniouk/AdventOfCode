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
    Planet orbit;
    List<Planet> satellites;
    Set<Planet> orbits;
    int numberOfOrbits = -1;
    String name;

    public Planet(String name) {
        this.name = name;
        satellites = new ArrayList<>();
        orbits = new HashSet<>();
    }

    public void setOrbit(Planet planet) {
        orbit = planet;
    }

    public void addOrbit(Planet p) {
        orbits.add(p);
        orbits.addAll(p.orbits);
//        System.out.println("Added " + p + p.orbits);
    }

    public void addSatellite(Planet p) {
        satellites.add(p);
    }

    @Override
    public String toString() {
        return name;
    }

    public int countAllOrbitsRecursive() {
        return numberOfOrbits = (numberOfOrbits == -1 ? (orbit == null ? 0 : 1 + orbit.countAllOrbitsRecursive()) : numberOfOrbits);
    }

    public void addOrbitsRecursive() {
//        System.out.println("Currently on " + name);
        satellites.forEach(s -> {
//            System.out.println("Looking at sattelite " + s.name);
            s.addOrbit(this);
            s.addOrbitsRecursive();
        });
    }
}
