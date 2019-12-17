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

    private static long[] memory;

    public static void main(String... args) throws IOException, InterruptedException {
        memory = Arrays.stream(Files.lines(Paths.get("input11.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
        part1();
        part2();
    }

    private static void part1() throws InterruptedException {
        Robot robot = new Robot(new IntcodeComputer(memory, LogLevel.DEBUG), 0, LogLevel.DEBUG);
        robot.runComputer();
        System.out.println("#tiles visited: " + robot.getTraversedPanels().size());
    }

    private static void part2() throws InterruptedException {
        Robot robot = new Robot(new IntcodeComputer(memory, LogLevel.QUIET), 1, LogLevel.QUIET);
        robot.runComputer();
        robot.printAllPanels();
    }
}

class Robot {
    private final IntcodeComputer computer;
    private Coords currentPanel;
    private final Map<Coords, Integer> traversedPanels;
    private Direction facing = Direction.UP;
    private Logger logger;

    Robot(IntcodeComputer computer, int startingColor, LogLevel logging) {
        this.computer = computer;
        logger = new Logger(this, logging);
        traversedPanels = new HashMap<>();
        currentPanel = new Coords(0, 0);
        traversedPanels.put(currentPanel, startingColor);
    }

    void runComputer() throws InterruptedException {
        new Thread(computer).start();
        long[] input = new long[2];
        do {
            // Pass color of current cell
            logger.log(LogLevel.DEBUG, "Waiting until computer asks for input");
            if (computer.getInputQueue().take() == computer.POISON) break; // Throwaway
            computer.getInputQueue().put((long) traversedPanels.get(currentPanel));
            // Wait for output, update color and move
            logger.log(LogLevel.DEBUG, "Waiting for color and move output");
            if ((input[0] = computer.getOutputQueue().take()) == computer.POISON) break;
            if ((input[1] = computer.getOutputQueue().take()) == computer.POISON) break;
            traversedPanels.put(currentPanel, toIntExact(input[0]));
            move(input[1]);
            traversedPanels.putIfAbsent(currentPanel, 0);
        } while (computer.isRunning());
        logger.log(LogLevel.INFO, "Done running computer");
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

    enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}