package AdventOfCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
    private static final char H_BORDER = '=';
    private static final char V_BORDER = '|';

    private static long[] memory;
    private Logger logger;
    private Map<Coords, Character> gameBoardTiles = new HashMap<>();
    private final Coords startingPosition = new Coords(GAMEBOARD_WIDTH/2, GAMEBOARD_HEIGHT/2);
    private Coords droidPosition = startingPosition;
    private char droidChar = DROID;

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
        IntcodeComputer computer = new IntcodeComputer(memory, LogLevel.IO);
        new GameGUI(computer, false).buildAndShowGUI();
        new Thread(computer).start();
    }

    private String getGameBoard() {
        StringBuilder builder = new StringBuilder();
        Character c;

        logger.debug("droidPosition: " + droidPosition + ", facing " + gameBoardTiles.get(droidPosition));

        int lowest;
        if (droidPosition.y > GAMEBOARD_HEIGHT-3) lowest = droidPosition.y - (GAMEBOARD_HEIGHT-3);
        else if (droidPosition.y < 1) lowest = droidPosition.y-2;
        else lowest = 0;

        int leftest;
        if (droidPosition.x > GAMEBOARD_WIDTH-3) leftest = droidPosition.x - (GAMEBOARD_WIDTH-3);
        else if (droidPosition.x < 1) leftest = droidPosition.x - 2;
        else leftest = 0;

        logger.debug("lowest: " + lowest + " , Leftest: " + leftest);
        for (int y = GAMEBOARD_HEIGHT-2+lowest; y >= lowest; y--) {
            // Print left side border
            builder.append(V_BORDER);
            for (int x = leftest; x < GAMEBOARD_WIDTH-2-leftest; x++) {
                if ((c = gameBoardTiles.get(new Coords(x, y))) != null) builder.append(c);
                else builder.append(UNKNOWN);
            }
            // Print right side border
            builder.append(V_BORDER + "\n");
        }
        // Print upper and lower border
        for (int x = 0; x < GAMEBOARD_WIDTH; x++) {
            builder.append(H_BORDER);
            builder.insert(0, H_BORDER);
        }
        builder.insert(GAMEBOARD_WIDTH, "\n");
        logger.log(LogLevel.RIDICULOUS, builder.toString());
        return builder.toString();
    }

    private String printFullGameBoard() {
        StringBuilder builder = new StringBuilder();
        Character c;
        int lowest   = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.y)).get().y;
        int highest  = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.y)).get().y;
        int leftest  = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.x)).get().x;
        int rightest = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.x)).get().x;

        for (int y = highest+2; y >= lowest-2; y--) {
            // Print left side border
            builder.append(V_BORDER);
            for (int x = leftest-2; x < rightest+2; x++) {
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
                gameBoardTiles.put(droidPosition, EMPTY);
                gameBoardTiles.put(getTileInFrontOfDroid(input), OX_SYST);
                droidPosition = getTileInFrontOfDroid(input);
                break;
        }
        gameBoardTiles.put(startingPosition, DROID);
        gameBoardTiles.put(droidPosition, droidChar);
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

    private int getRight(int direction) {
        switch (direction) {
            case 1: return 4;
            case 2: return 3;
            case 3: return 1;
            case 4: return 2;
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
        private boolean playWithUI;
        private int facing = 1;

        GameGUI(IntcodeComputer computer) {
            this.computer = computer;
            playWithUI = false;
        }

        GameGUI(IntcodeComputer computer, boolean playWithUI) {
            this(computer);
            this.playWithUI = playWithUI;
        }

        private void step(int direction) {
            processStatus(toIntExact(sendInput(direction)), direction);
        }

        private long cheatStep(int direction) {
            // Do given direction
            long status = sendInput(direction);
            processStatus(toIntExact(status), direction);
            // Look into other direction
            logger.debug("Looking into other directions");
            IntStream.of(1, 2, 3, 4).forEach(i -> {
                long innerStatus = sendInput(i);
                processStatus(toIntExact(innerStatus), i);
                if (innerStatus == 1 || innerStatus == 2) {
                    // We moved, backtrack
                    int oppositeDirection = getOppositeDirection(i);
                    processStatus(toIntExact(sendInput(oppositeDirection)), oppositeDirection);
                }
            });
            logger.debug("CheatStep done");
            return status;
        }

        private void UIStep() {
            logger.debug("Facing " + getDirectionName(facing) +
                    ", trying to go " + getDirectionName(getLeft(facing)));
            if (cheatStep(getLeft(facing)) == 0) {
                logger.debug("Couldn't go " + getDirectionName(getLeft(facing)) +
                        ", trying to go straight (" + getDirectionName(facing) + ")");
                if (cheatStep(facing) == 0) setFacing(getRight(facing));
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
                if (computer.getInputQueue().take() == computer.POISON) close();
                computer.getInputQueue().put((long) input);
                if ((output = computer.getOutputQueue().take()) == computer.POISON) close();
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
            mainArea = new JTextArea(GAMEBOARD_HEIGHT+1, GAMEBOARD_WIDTH+1);
            mainArea.setEditable(false);
            mainArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));
            mainArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            mainArea.setText(getGameBoard());
            mainArea.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}
                @Override
                public void keyPressed(KeyEvent e) {
                    if (playWithUI) {
                        UIStep();
                        updateGUI(getGameBoard());
                        return;
                    }
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            droidChar = DROID_LEFT;
                            cheatStep(3);
                            break;
                        case KeyEvent.VK_RIGHT :
                            droidChar = DROID_RIGHT;
                            cheatStep(4);
                            break;
                        case KeyEvent.VK_UP:
                            droidChar = DROID_UP;
                            cheatStep(1);
                            break;
                        case KeyEvent.VK_DOWN:
                            droidChar = DROID_DOWN;
                            cheatStep(2);
                            break;
                    }
                    updateGUI(getGameBoard());
                }
                @Override
                public void keyReleased(KeyEvent e) { }
            });
            JButton printButton = new JButton("Print Map");
            printButton.addActionListener(e -> {
                System.out.println("\n\n\n");
                System.out.println(printFullGameBoard());
                System.out.println("\n");
            });
            mainComp.add(mainArea, BorderLayout.CENTER);
            mainComp.add(printButton, BorderLayout.SOUTH);
            frame.getContentPane().add(mainComp);
            frame.pack();

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation((dim.width-frame.getWidth())/2, (dim.height-frame.getHeight())/2);

            frame.setVisible(true);
        }

        void updateGUI(String text) {
            mainArea.setText(text);
            frame.validate();
        }

        void close() {
            frame.dispose();
        }
    }
}
