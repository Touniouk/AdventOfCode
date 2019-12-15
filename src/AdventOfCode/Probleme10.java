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
        asteroidList.forEach(Asteroid::buildVisibleList);
        // Get the one with the most other visible asteroids
//        asteroidList.forEach(a -> System.out.println(a + " -> " + a.getVisibleList()));
//        System.out.println("==================");
        Asteroid best = asteroidList.stream().max(Comparator.comparingInt(a -> a.getVisibleList().size())).get();
        System.out.println("The best positioned asteroid is " + best + " with " + best.getVisibleList().size() + " other asteroids visible");
    }
}

class Asteroid {
    private Coords coords;
    private Map<Asteroid, Coords> distanceMap;
    private Map<Double, Set<Asteroid>> angleMap;
    private List<Asteroid> visibleList;

    Asteroid(int x, int y) {
        coords = new Coords(x, y);
    }

    void buildDistanceMap(List<Asteroid> asteroids) {
        distanceMap = new HashMap<>();
        asteroids.forEach(a -> distanceMap.put(a, Coords.minus(a.coords, this.coords)));
    }

    void buildAngleMap(List<Asteroid> asteroids) {
        angleMap = new HashMap<>();
        for (Asteroid a : asteroids) {
            double angle, x = distanceMap.get(a).x, y = distanceMap.get(a).y;
            if (x == 0 && y == 0) continue;
            else if (x == 0 && y > 0) angle = 90d;
            else if (x == 0 && y < 0) angle = -90d;
            else if (y == 0 && x < 0) angle = 180;
            else angle = Math.toDegrees(Math.atan((double) distanceMap.get(a).y / (double) distanceMap.get(a).x));
//            if (x > 0 && y < 0) angle += 180;
            if (x < 0 && y > 0) angle += 180;
            else if (x < 0 && y < 0) angle -= 180;
            angleMap.computeIfAbsent(angle, k -> new HashSet<>());
            angleMap.get(angle).add(a);
        }
    }

    void buildVisibleList() {
        visibleList = new ArrayList<>();
        angleMap.forEach((k,v) -> visibleList.add(v.stream().min((a1, a2) -> Coords.distanceDifference(distanceMap.get(a1), distanceMap.get(a2))).get()));
    }

    public List<Asteroid> getVisibleList() {
        return visibleList;
    }

    public Map<Double, Set<Asteroid>> getAngleMap() {
        return angleMap;
    }

    public Map<Asteroid, Coords> getDistanceMap() {
        return distanceMap;
    }

    @Override
    public String toString() {
        return coords.toString();
    }
}

class Coords {
    int x;
    int y;

    Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    static Coords minus(Coords c1, Coords c2) {
        return new Coords(c1.x-c2.x, c1.y-c2.y);
    }

    static int distanceDifference(Coords c1, Coords c2) {
        return Math.abs(c1.x) + Math.abs(c1.y) - Math.abs(c2.x) - Math.abs(c2.y);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }
}