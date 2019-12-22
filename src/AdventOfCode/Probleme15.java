package AdventOfCode;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.toIntExact;

public class Probleme15 {
    private static final int GAMEBOARD_WIDTH = 56;
    private static final int GAMEBOARD_HEIGHT = 30;
    private static final char DROID = 'o';
    private static final char DROID_UP = '^';
    private static final char DROID_DOWN = 'v';
    private static final char DROID_LEFT = '<';
    private static final char DROID_RIGHT = '>';
    private static final char WALL = '█';
    private static final char EMPTY = ' ';
    private static final char UNKNOWN = '?';
    private static final char OX_SYST = '$';
    private static final char OXYGEN = 'O';
    private static final char V_BORDER = '|';

    private static long[] memory;
    private Logger logger;
    private GameGUI GUI;
    private Map<Coords, Character> gameBoardTiles = new HashMap<>();
    private final Coords startingPosition = new Coords(GAMEBOARD_WIDTH/2, GAMEBOARD_HEIGHT/2);
    private Coords droidPosition = startingPosition;
    private Coords oxygenPosition;
    private char droidChar = DROID;
    private Stack<Integer> inputs = new Stack<>();
    private boolean allMapExplored = false;

    public static void main(String... args) throws IOException {
        memory = Arrays.stream(Files.lines(Paths.get("input15.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
        Probleme15 p = new Probleme15();
        p.logger = new Logger(p, LogLevel.QUIET);
        p.solveWithGUI();
    }

    private void solveWithGUI() {
        // Put robot in the middle of the board
        gameBoardTiles.put(droidPosition, DROID);
        IntcodeComputer computer = new IntcodeComputer(memory, LogLevel.QUIET);
        GUI = new GameGUI(computer);
        GUI.buildAndShowGUI();
        new Thread(computer).start();

        while (!allMapExplored) {
            GUI.UIStep();
        }
        fillWithOxygen();
    }

    private String printFullGameBoard() {
        StringBuilder builder = new StringBuilder();
        Character c;
        int lowest   = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.y)).get().y;
        int highest  = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.y)).get().y;
        int leftest  = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.x)).get().x;
        int rightest = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.x)).get().x;

        for (int y = highest; y >= lowest; y--) {
            // Print left side border
            builder.append(V_BORDER);
            for (int x = leftest; x < rightest; x++) {
                if ((c = gameBoardTiles.get(new Coords(x, y))) != null) builder.append(c);
                else builder.append(UNKNOWN);
            }
            // Print right side border
            builder.append(V_BORDER + "\n");
        }
        return builder.toString();
    }

    private void processStatus(int tileId, int input) {
        switch (tileId) {
            case 0:
                // The repair droid hit a wall. Its position has not changed.
                gameBoardTiles.put(getTileInFrontOfDroid(input), WALL);
                break;
            case 1:
                // The repair droid has moved one step in the requested direction.
                gameBoardTiles.put(droidPosition, EMPTY);
                gameBoardTiles.put(getTileInFrontOfDroid(input), droidChar);
                droidPosition = getTileInFrontOfDroid(input);
                break;
            case 2:
                // The repair droid has moved one step in the requested direction;
                // its new position is the location of the oxygen system.
                oxygenPosition = getTileInFrontOfDroid(input);
                gameBoardTiles.put(droidPosition, EMPTY);
                gameBoardTiles.put(getTileInFrontOfDroid(input), OX_SYST);
                droidPosition = getTileInFrontOfDroid(input);
                break;
        }
        gameBoardTiles.put(startingPosition, DROID);
        if (oxygenPosition != null) gameBoardTiles.put(oxygenPosition, OX_SYST);
        gameBoardTiles.put(droidPosition, droidChar);
        if (droidPosition.equals(startingPosition) && gameBoardTiles.size() > 10) {
            logger.info("Full Map explored");
            allMapExplored = true;
        }
    }

    private void fillWithOxygen() {
        logger.debug("Filling with Oxygen");
        Queue<Coords> edges = new ArrayDeque<>();
        Queue<Coords> oldEdges = new ArrayDeque<>();
        edges.add(oxygenPosition);
        int time = -1;
        while (!edges.isEmpty()) {
            while (!edges.isEmpty()) oldEdges.add(edges.poll());
            while (!oldEdges.isEmpty()) {
                Coords edge = oldEdges.poll();
                Coords[] surroundings = new Coords[] {
                        new Coords(edge.x + 1, edge.y),
                        new Coords(edge.x - 1, edge.y),
                        new Coords(edge.x, edge.y - 1),
                        new Coords(edge.x, edge.y + 1)
                };
                for (Coords surrounding : surroundings) {
                    if (gameBoardTiles.get(surrounding) != null && gameBoardTiles.get(surrounding) == EMPTY) {
                        edges.add(surrounding);
                        gameBoardTiles.put(surrounding, OXYGEN);
                    }
                }
            }
            time++;
            GUI.timeLabel.setText("Time: " + time + " minutes");
            GUI.updateGUI(printFullGameBoard());
        }
    }

    private int getOppositeDirection(int direction) {
        switch (direction) {
            case 1: return 2;
            case 2: return 1;
            case 3: return 4;
            case 4: return 3;
        }
        return 0;
    }

    private int getLeft(int direction) {
        switch (direction) {
            case 1: return 3;
            case 2: return 4;
            case 3: return 2;
            case 4: return 1;
        }
        return 0;
    }

    private String getDirectionName(int direction) {
        switch (direction) {
            case 1: return "NORTH (" + DROID_UP + ")";
            case 2: return "SOUTH (" + DROID_DOWN + ")";
            case 3: return "WEST (" + DROID_LEFT + ")";
            case 4: return "EAST (" + DROID_RIGHT + ")";
        }
        return "UNKNOWN";
    }

    private Coords getTileInFrontOfDroid(int facing) {
        switch (facing) {
            case 1: return new Coords(droidPosition.x, droidPosition.y+1); // NORTH
            case 2: return new Coords(droidPosition.x, droidPosition.y-1); // SOUTH
            case 3: return new Coords(droidPosition.x-1, droidPosition.y); // WEST
            case 4: return new Coords(droidPosition.x+1, droidPosition.y); // EAST
        }
        return droidPosition;
    }

    private class GameGUI {
        private IntcodeComputer computer;
        private JFrame frame;
        private JTextArea mainArea;
        private JLabel stepsLabel;
        private JLabel timeLabel;
        private int facing = 1;

        GameGUI(IntcodeComputer computer) {
            this.computer = computer;
        }

        private long step(int direction) {
            long status = sendInput(direction);
            processStatus(toIntExact(status), direction);
            if (status == 1  || status == 2) updateGUI(printFullGameBoard());
            return status;
        }

        private void UIStep() {
            logger.debug("Facing " + getDirectionName(facing) +
                    ", trying to go " + getDirectionName(getLeft(facing)));
            if (step(getLeft(facing)) == 0) {
                logger.debug("Couldn't go " + getDirectionName(getLeft(facing)) +
                        ", trying to go straight (" + getDirectionName(facing) + ")");
                if (step(facing) == 0) setFacing(getOppositeDirection(getLeft(facing)));
            } else setFacing(getLeft(facing));
            logger.debug("UIStep done, facing " + getDirectionName(facing));
        }

        private void setFacing(int direction) {
            facing = direction;
            switch (direction) {
                case 1: droidChar = DROID_UP; break;
                case 2: droidChar = DROID_DOWN; break;
                case 3: droidChar = DROID_LEFT; break;
                case 4: droidChar = DROID_RIGHT; break;
            }
            logger.debug("Now facing " + getDirectionName(direction));
            gameBoardTiles.put(droidPosition, droidChar);
        }

        private long sendInput(int input) {
            long output = computer.POISON;
            try {
                computer.getInputQueue().take();
                computer.getInputQueue().put((long) input);
                output = computer.getOutputQueue().take();
                if (output > 0 && oxygenPosition != null) {
                    if (inputs.isEmpty() || inputs.peek() != getOppositeDirection(input)) inputs.push(input);
                    else inputs.pop();
                    stepsLabel.setText("Steps: " + inputs.size());
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return output;
        }

        private void buildAndShowGUI() {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            JPanel mainComp = new JPanel(new BorderLayout());
            mainArea = new JTextArea(1, 1);
            mainArea.setEditable(false);
            mainArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));
            mainArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            mainArea.setText(printFullGameBoard());
            mainComp.add(mainArea, BorderLayout.CENTER);
            JPanel northPanel = new JPanel(new GridLayout(1, 2));
            timeLabel = new JLabel("Time: 0 minutes");
            stepsLabel = new JLabel("Steps: ");
            northPanel.add(stepsLabel);
            northPanel.add(timeLabel);
            mainComp.add(northPanel, BorderLayout.NORTH);
            frame.getContentPane().add(mainComp);
            frame.pack();
            frame.setVisible(true);
        }

        void updateGUI(String text) {
            mainArea.setText(text);
            frame.validate();
            frame.setSize(frame.getPreferredSize());
        }
    }
}
