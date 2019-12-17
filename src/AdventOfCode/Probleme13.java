package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class Probleme13 {

    private static final Map<Coords, String> gameBoardTiles = new HashMap<>();
    private static long[] memory;

    public static void main(String... args) throws IOException, InterruptedException {
        memory = Arrays.stream(Files.lines(Paths.get("input13.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
//        part1();
//        printGameBoard();
        part2();
    }

    private static void part1() throws InterruptedException {
        IntcodeComputer computer= new IntcodeComputer(memory, LoggingLevel.QUIET);
        runComputer(computer);
        System.out.println("There are " +
                gameBoardTiles.entrySet().stream().filter(e -> e.getValue().equals("@")).count()
        + " block tiles");
    }

    private static void part2() throws InterruptedException {
        long[] memory = Arrays.copyOf(Probleme13.memory, Probleme13.memory.length);
        memory[0] = 2;
        IntcodeComputer computer = new IntcodeComputer(memory, LoggingLevel.IO);
        runComputer(computer);
    }

    private static void runComputer(IntcodeComputer computer) throws InterruptedException {
        new Thread(computer).start();
        int[] input = new int[3];
        String tile;
        do {
            // Get all three outputs
            System.out.println("Waiting for output");
            if (
                    (input[0] = toIntExact(computer.getOutputQueue().take())) == computer.POISON ||
                    (input[1] = toIntExact(computer.getOutputQueue().take())) == computer.POISON ||
                    (input[2] = toIntExact(computer.getOutputQueue().take())) == computer.POISON) break;
            // Add the tile to the list
            if (input[0] == -1 && input[1] == 0) {
                // Input joystick position
                printGameBoard();
            }
            else gameBoardTiles.put(new Coords(input[0], input[1]), getTile(input[2]));
        } while (computer.isRunning());
    }

    private static String getTile(int tileId) {
        switch (tileId) {
            case 0: return " ";
            case 1: return "#";
            case 2: return "@";
            case 3: return "-";
            case 4: return "o";
        }
        return String.valueOf(tileId);
    }

    private static void printGameBoard() {
        // Get highest, lowest, most to the right and most to the left panels
        int lowest   = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.y)).get().y;
        int highest  = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.y)).get().y;
        int leftest  = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.x)).get().x;
        int rightest = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.x)).get().x;

        String temp;
        for (int y = highest+1; y >= lowest-1; y--) {
            for (int x = leftest - 1; x <= rightest + 1; x++) {
                if ((temp = gameBoardTiles.get(new Coords(x, y))) != null) System.out.print(temp);
            }
            System.out.println();
        }
    }
}
