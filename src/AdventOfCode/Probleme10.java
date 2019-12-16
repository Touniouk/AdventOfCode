package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
        // Get the one with the most other visible asteroids
        Asteroid best = asteroidList.stream().max(Comparator.comparingInt(a -> a.getAngleMap().size())).get();
        Asteroid vaporizedAsteroid = best.getVaporizedAsteroid(200, false);
        System.out.println("The best positioned asteroid is " + best + " with " + best.getAngleMap().size() + " other asteroids visible");
        System.out.println("The 200th asteroid to be vaporized is " + vaporizedAsteroid + " (" + vaporizedAsteroid.getNumberRepresentation() + ")");
    }
}

class Asteroid {
    private Coords coords;
    private Map<Asteroid, Coords> distanceMap;
    private Map<Double, Queue<Asteroid>> angleMap;

    Asteroid(int x, int y) {
        coords = new Coords(x, y);
    }

    void buildDistanceMap(List<Asteroid> asteroids) {
        distanceMap = new HashMap<>();
        asteroids.forEach(a -> distanceMap.put(a, Coords.minus(a.coords, this.coords)));
    }

    void buildAngleMap(List<Asteroid> asteroids) {
        angleMap = new TreeMap<>();
        for (Asteroid a : asteroids) {
            double angle, x = distanceMap.get(a).x, y = distanceMap.get(a).y;
            if (x == 0 && y == 0) continue;
            else if (x == 0 && y < 0) angle = 0d;
            else if (x == 0 && y > 0) angle = 180d;
            else {
                angle = Math.toDegrees(Math.atan((double) distanceMap.get(a).x / (double) distanceMap.get(a).y));
                if (y >= 0) angle = 180 - angle;
                else if (x > 0) angle *= -1;
                else if (x < 0) angle = 360 - angle;
            }
            angleMap.computeIfAbsent(angle, k -> new PriorityQueue<>((a1, a2) -> Coords.distanceDifference(distanceMap.get(a1), distanceMap.get(a2))));
            angleMap.get(angle).add(a);
        }
    }

    Asteroid getVaporizedAsteroid(int number, boolean print) {
        boolean allDestroyed = false;
        int counter = 0;
        Map<Double, Queue<Asteroid>> angleMapCopy = new TreeMap<>(angleMap);
        while (!allDestroyed) {
            allDestroyed = true;
            for (Map.Entry<Double, Queue<Asteroid>> entry : angleMapCopy.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    counter++;
                    if (print) System.out.println(counter + " - " + entry.getValue().peek());
                    if (counter == number) return entry.getValue().peek();
                    entry.getValue().poll();
                    allDestroyed = false;
                }
            }
        }
        return null;
    }

    Map<Double, Queue<Asteroid>> getAngleMap() {
        return angleMap;
    }

    int getNumberRepresentation() {
        return coords.x * 100 + coords.y;
    }

    @Override
    public String toString() {
        return coords.toString();
    }
}

class Coords {
    final int x;
    final int y;

    Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    static Coords minus(Coords c1, Coords c2) {
        return new Coords(c1.x-c2.x, c1.y-c2.y);
    }

    static int distanceDifference(Coords c1, Coords c2) {
        return Math.abs(c1.x) + Math.abs(c1.y) - (Math.abs(c2.x) + Math.abs(c2.y));
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coords coords = (Coords) o;
        return x == coords.x && y == coords.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}