package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Probleme3 {
    public static void main(String... args) throws IOException {
        new Probleme3().solve();
    }

    void solve() throws IOException {
        List<Wire> wires = Files.lines(Paths.get("input3.txt"))
                .map(line -> new Wire(line.split(",")))
                .collect(Collectors.toList());
        HashSet<Coords> set = new HashSet<>(wires.get(0).points.keySet());
        set.retainAll(new HashSet<>(wires.get(1).points.keySet()));
        Coords closest = set.stream().min(
                (p1, p2) -> Math.abs(p1.x) + Math.abs(p1.y) - Math.abs(p2.x) - Math.abs(p2.y)
        ).get();
        Coords lowestDelay = set.stream().min(
                (p1, p2) -> wires.get(0).points.get(p1) + wires.get(1).points.get(p1) -
                        wires.get(0).points.get(p2) - wires.get(1).points.get(p2)
        ).get();
        System.out.println("Closest point: " + closest + " : " + (Math.abs(closest.x) + Math.abs(closest.y)));
        System.out.println("Lowest delay point: " + lowestDelay + " : " + (wires.get(0).points.get(lowestDelay) + wires.get(1).points.get(lowestDelay)));
    }

    class Wire {
        HashMap<Coords, Integer> points;

        Wire(String[] bendStr) {
            points = new HashMap<>();
            List<Bend> bends = Arrays.stream(bendStr).map(Bend::new).collect(Collectors.toList());
            int xcoord = 0, ycoord = 0, totalDistCounter = 1;
            for (Bend bend : bends) {
                int distCounter = bend.distance, incrementx = 0, incrementy = 0;
                switch (bend.direction) {
                    case "U":
                        incrementx = 0;
                        incrementy = 1;
                        break;
                    case "D":
                        incrementx = 0;
                        incrementy = -1;
                        break;
                    case "R":
                        incrementx = 1;
                        incrementy = 0;
                        break;
                    case "L":
                        incrementx = -1;
                        incrementy = 0;
                }
                while (distCounter > 0) {
                    points.putIfAbsent(new Coords((xcoord += incrementx), (ycoord += incrementy)), totalDistCounter);
                    distCounter--;
                    totalDistCounter++;
                }
            }
        }
    }

    class Bend {
        String direction;
        int distance;

        Bend(String str) {
            direction = str.substring(0, 1);
            distance = Integer.parseInt(str.substring(1));
        }
    }
}
