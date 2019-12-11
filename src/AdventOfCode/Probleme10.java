package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Probleme10 {
    public static void main(String... args) throws IOException {
        List<String> lines = Files.lines(Paths.get("input10.txt")).collect(Collectors.toList());
        List<Asteroid> asteroidList = new ArrayList<>();
        // Creating the asteroids
        for (int i = 0; i < lines.size(); i++) {
            char[] carr = lines.get(i).toCharArray();
            for (int j = 0; j < carr.length; j++) if (carr[j] == '#') asteroidList.add(new Asteroid(j, i));
        }
        // Building angle maps
        asteroidList.forEach(a -> a.buildDistanceMap(asteroidList));
        asteroidList.forEach(a -> a.buildAngleMap(asteroidList));

        System.out.println(asteroidList.get(0).distanceMap);
        System.out.println("Asteroid location: " +  asteroidList.get(0));
        for (Map.Entry e : asteroidList.get(0).angleMap.entrySet()) {
            for (Asteroid a : (Set<Asteroid>) e.getValue()) {
                System.out.println("Asteroid " + a + ", angle " + e.getKey() + ", distance " + asteroidList.get(0).distanceMap.get(a));
            }
        }
    }
}

class Asteroid {
    Coords coords;
    Map<Asteroid, Coords> distanceMap;
    Map<Double, Set<Asteroid>> angleMap;

    Asteroid(int x, int y) {
        angleMap = new HashMap<>();
        distanceMap = new HashMap<>();
        coords = new Coords(x, y);
    }

    void buildDistanceMap(List<Asteroid> asteroids) {
        asteroids.forEach(a -> distanceMap.put(a, a.coords.minus(this.coords)));
    }

    void buildAngleMap(List<Asteroid> asteroids) {
        asteroids.forEach(a -> {
            double angle;
            if (distanceMap.get(a).x == 0) angle = 90d;
            else angle = Math.toDegrees(Math.atan(distanceMap.get(a).y / distanceMap.get(a).x));
            angleMap.computeIfAbsent(angle, k -> new HashSet<>());
            angleMap.get(angle).add(a);
        });
    }

    @Override
    public String toString() {
        return coords.toString();
    }

    class Coords {
        int x;
        int y;

        Coords(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Coords minus(Coords coord) {
            return new Coords(this.x-coord.x, this.y-coord.y);
        }

        Coords divide(Coords coord) {
            return new Coords(this.x/coord.x, this.y/coord.y);
        }

        boolean isMultiple(Coords coord) {
            Coords factor = this.divide(coord);
            return factor.x == factor.y;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }
}