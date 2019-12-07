package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Probleme1 {
    public static void main(String... args) throws IOException {
        int res = Files.lines(Paths.get("input1.txt"))
                .mapToInt(Integer::parseInt)
                .map(Probleme1::calculateFuelForModule)
                .sum();
        System.out.println(res);
    }

    private static int calculateFuelForModule(Integer input) {
        int fuel = input / 3 - 2, totalFuel = 0;
        while (fuel > 0) {
            totalFuel += fuel;
            fuel = fuel / 3 - 2;
        }
        return totalFuel;
    }
}
