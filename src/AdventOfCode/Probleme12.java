package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Probleme12 {
    public static void main(String... args) throws IOException {
        List<Moon> moons = new ArrayList<>(4);
        Files.lines(Paths.get("input12.txt")).forEach(l -> {
            String[] s = l.substring(1, l.length() - 1).split(", ");
            moons.add(new Moon(
                    Integer.parseInt(s[0].substring(2)),
                    Integer.parseInt(s[1].substring(2)),
                    Integer.parseInt(s[2].substring(2))));
        });
        part1(moons);
        part2(moons);
    }

    private static void part1(List<Moon> moons) {
        System.out.println("Step 0 | " + moons);
        for (int i = 1; i <= 1000; i++) {
            step(moons);
            System.out.println("Step " + i + " | " + moons);
        }
        System.out.println(moons.stream().mapToInt(Moon::calculateEnergy).sum());
    }

    private static void part2(List<Moon> moons) {
        // TODO
        Coord3d nullVelocity = new Coord3d(0, 0, 0);
        long counter = 0;
        while (true) {
            step(moons);
            counter++;
            if (counter%1000000 == 0) System.out.println("Step " + counter + " | " + moons);
            if (moons.get(0).velocity.equals(nullVelocity) &&
                    moons.get(0).velocity.equals(nullVelocity) &&
                    moons.get(0).velocity.equals(nullVelocity)) {
                System.out.println("Step " + counter + " | " + moons);
                return;
            }
        }
    }

    static void step(List<Moon> moons) {
        moons.forEach(m -> moons.forEach(mInner -> {
            if      (m.position.x > mInner.position.x) m.velocity.x--;
            else if (m.position.x < mInner.position.x) m.velocity.x++;
            if      (m.position.y > mInner.position.y) m.velocity.y--;
            else if (m.position.y < mInner.position.y) m.velocity.y++;
            if      (m.position.z > mInner.position.z) m.velocity.z--;
            else if (m.position.z < mInner.position.z) m.velocity.z++;
        }));
        moons.forEach(moon -> {
            moon.position.x += moon.velocity.x;
            moon.position.y += moon.velocity.y;
            moon.position.z += moon.velocity.z;
        });
    }
}

class Moon {
    Coord3d position;
    Coord3d velocity;

    Moon(int x, int y, int w) {
        position = new Coord3d(x, y, w);
        velocity = new Coord3d(0, 0, 0);
    }

    int calculateEnergy() {
        return (Math.abs(position.x) + Math.abs(position.y) + Math.abs(position.z)) *
                (Math.abs(velocity.x) + Math.abs(velocity.y) + Math.abs(velocity.z));
    }

    @Override
    public String toString() {
        return "Position=" + position + " | velocity=" + velocity;
    }
}

class Coord3d {

    int x;
    int y;
    int z;

    Coord3d(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "[" + (x < 0 ? x : " "+x) + "," + (y < 0 ? y : " "+y) + "," + (z < 0 ? z : " "+z) + "]";
    }
}
