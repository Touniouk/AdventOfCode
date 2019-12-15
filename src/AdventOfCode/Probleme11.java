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
        Robot robot = new Robot();
        robot.start();
    }
}

class Robot implements Operator {
    private IntcodeComputer computer;
    int[] currentPanel;
    private Map<int[], Integer> traversedPanels;
    Direction facing = Direction.UP;

    Robot() throws IOException {
        computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input11.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray(), this, true);
        traversedPanels = new HashMap<>();
        currentPanel = new int[] {0, 0};
        addPanel(currentPanel, 0);
    }

    void start() throws InterruptedException {
        new Thread(computer).start();
        while (true) {
            // Pass color of current cell
            computer.addInput(traversedPanels.get(currentPanel));
            // Wait for output, update color
            traversedPanels.put(currentPanel, toIntExact(waitForOutput()));
            // Wait for output, move in specified direction
            move(waitForOutput());
        }
    }

    private Direction move(long direction) {
        switch (facing) {
            case UP:    return (direction == 0) ? Direction.LEFT  : Direction.RIGHT;
            case DOWN:  return (direction == 0) ? Direction.RIGHT : Direction.LEFT;
            case RIGHT: return (direction == 0) ? Direction.UP    : Direction.DOWN;
            case LEFT:  return (direction == 0) ? Direction.DOWN  : Direction.UP;
        }
        return null;
    }

    private synchronized long waitForOutput() throws InterruptedException {
        wait();
        return computer.getOutputQueue().poll();
    }

    private void addPanel(int[] panel, int color) {
        traversedPanels.put(panel, color);
    }

    @Override
    public synchronized void nudge() {
        notify();
    }

    enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}