package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class Probleme11 {

    public static void main(String... args) throws IOException, InterruptedException {
        part1();
//        part2();

    }

    private static void part1() throws IOException, InterruptedException {
        Robot robot = new Robot(new IntcodeComputer(Arrays.stream(
                Files.lines(Paths.get("input11.txt")).findFirst().get().split(","))
                .mapToLong(Long::parseLong).toArray(), false), 0, false);
        robot.runComputer();
        System.out.println("#tiles visited: " + robot.getTraversedPanels().size());
    }

    private static void part2() throws IOException, InterruptedException {

    }
}

class Robot {
    private final IntcodeComputer computer;
    private Coords currentPanel;
    private final Map<String, Integer> traversedPanels;
    private Direction facing = Direction.UP;
    private boolean logging;

    Robot(IntcodeComputer computer, int startingColor, boolean logging) {
        this.computer = computer;
        this.logging = logging;
        traversedPanels = new HashMap<>();
        currentPanel = new Coords(0, 0);
        traversedPanels.put(currentPanel.toString(), startingColor);
    }

    void runComputer() throws InterruptedException {
        new Thread(computer).start();
        long input;
        do {
            // Pass color of current cell
            computer.getInputQueue().put((long) traversedPanels.get(currentPanel.toString()));
            // Wait for output, update color
            log("Waiting for color output");
            if ((input = computer.getOutputQueue().take()) == computer.POISON)break;
            traversedPanels.put(currentPanel.toString(), toIntExact(input));
            // Wait for output, move in specified direction
            log("waiting for move output");
            if ((input = computer.getOutputQueue().take()) == computer.POISON)break;
            move(input);
            traversedPanels.putIfAbsent(currentPanel.toString(), 0);
        } while (computer.isRunning());
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

    public Map<String, Integer> getTraversedPanels() {
        return traversedPanels;
    }

    private void log(String logMessage) {
        if (logging) System.out.println("> Robot: " + logMessage);
    }

    enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}