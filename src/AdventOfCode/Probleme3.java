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
        HashSet<Point> set = new HashSet<>(wires.get(0).points.keySet());
        set.retainAll(new HashSet<>(wires.get(1).points.keySet()));
        Point closest = set.stream().min(
                (p1, p2) -> Math.abs(p1.xcoord) + Math.abs(p1.ycoord) - Math.abs(p2.xcoord) - Math.abs(p2.ycoord)
        ).get();
        Point lowestDelay = set.stream().min(
                (p1, p2) -> wires.get(0).points.get(p1) + wires.get(1).points.get(p1) -
                        wires.get(0).points.get(p2) - wires.get(1).points.get(p2)
        ).get();
        System.out.println("Closest point: " + closest + " : " + (Math.abs(closest.xcoord) + Math.abs(closest.ycoord)));
        System.out.println("Lowest delay point: " + lowestDelay + " : " + (wires.get(0).points.get(lowestDelay) + wires.get(1).points.get(lowestDelay)));
    }

    class Wire {
        HashMap<Point, Integer> points;

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
                    points.putIfAbsent(new Point((xcoord += incrementx), (ycoord += incrementy)), totalDistCounter);
                    distCounter--;
                    totalDistCounter++;
                }
            }
        }
    }

    class Point {
        int xcoord;
        int ycoord;

        Point(int xcoord, int ycoord) {
            this.xcoord = xcoord;
            this.ycoord = ycoord;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return xcoord == point.xcoord &&
                    ycoord == point.ycoord;
        }

        @Override
        public int hashCode() {
            return Objects.hash(xcoord, ycoord);
        }

        @Override
        public String toString() {
            return "[" + xcoord + "," + ycoord + "]";
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
