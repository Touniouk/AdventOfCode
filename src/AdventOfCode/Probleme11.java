package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class Probleme11 {

    public static void main(String... args) throws IOException, InterruptedException {
        part1();
        part2();
    }

    private static void part1() throws InterruptedException, IOException {
        Robot robot = new Robot(new IntcodeComputer(Arrays.stream(
                Files.lines(Paths.get("input11.txt")).findFirst().get().split(","))
                .mapToLong(Long::parseLong).toArray(), false), 0, false);
        robot.runComputer();
        System.out.println("#tiles visited: " + robot.getTraversedPanels().size());
    }

    private static void part2() throws InterruptedException, IOException {
        Robot robot = new Robot(new IntcodeComputer(Arrays.stream(
                Files.lines(Paths.get("input11.txt")).findFirst().get().split(","))
                .mapToLong(Long::parseLong).toArray(), false), 1, false);
        robot.runComputer();
        robot.printAllPanels();
    }
}

class Robot {
    private final IntcodeComputer computer;
    private Coords currentPanel;
    private final Map<Coords, Integer> traversedPanels;
    private Direction facing = Direction.UP;
    private boolean logging;

    Robot(IntcodeComputer computer, int startingColor, boolean logging) {
        this.computer = computer;
        this.logging = logging;
        traversedPanels = new HashMap<>();
        currentPanel = new Coords(0, 0);
        traversedPanels.put(currentPanel, startingColor);
    }

    void runComputer() throws InterruptedException {
        new Thread(computer).start();
        long input;
        do {
            // Pass color of current cell
            computer.getInputQueue().put((long) traversedPanels.get(currentPanel));
            // Wait for output, update color
            log("Waiting for color output");
            if ((input = computer.getOutputQueue().take()) == computer.POISON)break;
            traversedPanels.put(currentPanel, toIntExact(input));
            // Wait for output, move in specified direction
            log("waiting for move output");
            if ((input = computer.getOutputQueue().take()) == computer.POISON)break;
            move(input);
            traversedPanels.putIfAbsent(currentPanel, 0);
        } while (computer.isRunning());
    }

    void printAllPanels() {
        // Get highest, lowest, most to the right and most to the left panels
        int lowest   = traversedPanels.keySet().stream().min(Comparator.comparingInt(k -> k.y)).get().y;
        int highest  = traversedPanels.keySet().stream().max(Comparator.comparingInt(k -> k.y)).get().y;
        int leftest  = traversedPanels.keySet().stream().min(Comparator.comparingInt(k -> k.x)).get().x;
        int rightest = traversedPanels.keySet().stream().max(Comparator.comparingInt(k -> k.x)).get().x;

        Integer temp;
        for (int y = highest+1; y >= lowest-1; y--) {
            for (int x = leftest - 1; x <= rightest + 1; x++) {
                if ((temp = traversedPanels.get(new Coords(x, y))) != null && temp == 1) System.out.print("#");
                else System.out.print(" ");
            }
            System.out.println();
        }
    }

    private void move(long direction) {
        if ((facing==Direction.UP && direction==0) || (facing==Direction.DOWN && direction==1)) goLeft();
        else if ((facing==Direction.UP && direction==1) || (facing==Direction.DOWN && direction==0)) goRight();
        else if ((facing==Direction.RIGHT && direction==0) || (facing==Direction.LEFT && direction==1)) goUp();
        else if ((facing==Direction.LEFT && direction==0) || (facing==Direction.RIGHT && direction==1)) goDown();
    }

    private void goLeft() {
        facing = Direction.LEFT;
        currentPanel = new Coords(currentPanel.x - 1, currentPanel.y);
    }

    private void goRight() {
        facing = Direction.RIGHT;
        currentPanel = new Coords(currentPanel.x + 1, currentPanel.y);
    }

    private void goDown() {
        facing = Direction.DOWN;
        currentPanel = new Coords(currentPanel.x, currentPanel.y - 1);
    }

    private void goUp() {
        facing = Direction.UP;
        currentPanel = new Coords(currentPanel.x, currentPanel.y + 1);
    }

    Map<Coords, Integer> getTraversedPanels() {
        return traversedPanels;
    }

    private void log(String logMessage) {
        if (logging) System.out.println("> Robot: " + logMessage);
    }

    enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}