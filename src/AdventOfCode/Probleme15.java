package AdventOfCode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
    private static final int GAMEBOARD_WIDTH = 43;
    private static final int GAMEBOARD_HEIGHT = 25;
    private static final char DROID = 'o';
    private static final char DROID_UP = '^';
    private static final char DROID_DOWN = 'v';
    private static final char DROID_LEFT = '<';
    private static final char DROID_RIGHT = '>';
    private static final char WALL = '#';
    private static final char EMPTY = '.';
    private static final char UNKNOWN = ' ';
    private static final char OX_SYST = '$';
    private static final char H_BORDER = '=';
    private static final char V_BORDER = '|';

    private static long[] memory;
    private Logger logger;
    private Map<Coords, Character> gameBoardTiles = new HashMap<>();
    private Coords droidPosition;
//    private int gameBoardWidth = GAMEBOARD_WIDTH;
//    private int gameBoardHeight = GAMEBOARD_HEIGHT;
    private char droidChar = DROID;
    private GameGUI GUI;

    public static void main(String... args) throws IOException, InterruptedException {
        memory = Arrays.stream(Files.lines(Paths.get("input15.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
        Probleme15 p = new Probleme15();
        p.logger = new Logger(p, LogLevel.DEBUG);
        p.solveWithGUI();
    }

    private void solveWithGUI() {
        // Put robot in the middle of the board
        droidPosition = new Coords(GAMEBOARD_WIDTH/2, GAMEBOARD_WIDTH/2);
        gameBoardTiles.put(droidPosition, DROID);

        IntcodeComputer computer = new IntcodeComputer(memory, LogLevel.IO);

        GUI = new GameGUI(computer);
        GUI.buildAndShowGUI();

        new Thread(computer).start();

        logger.debug("END");
    }

    private String getGameBoard() {
        StringBuilder builder = new StringBuilder();
        Character c;

        logger.debug("droidPosition: " + droidPosition);

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
        gameBoardTiles.put(droidPosition, droidChar);
    }

    private void processStatusCheat(int tileId, int input) {
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
        gameBoardTiles.put(droidPosition, droidChar);
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

        GameGUI(IntcodeComputer computer) {
            this.computer = computer;
        }

        private void step(int direction) {
            mainArea.setEnabled(false);
            processStatus(toIntExact(sendInput(direction)), direction);
            updateGUI(getGameBoard());
            mainArea.setEnabled(true);
        }

        private void cheatStep(int direction) {
            mainArea.setEnabled(false);
            // Do given direction
            processStatus(toIntExact(sendInput(direction)), direction);
            // Look into other direction
            IntStream.of(1, 2, 3, 4).forEach(i -> {
                long status = sendInput(i);
                processStatus(toIntExact(status), i);
                if (status == 1 || status == 2) {
                    // We moved, backtrack
                    switch (i) {
                        case 1: processStatus(toIntExact(sendInput(2)), 2); break;
                        case 2: processStatus(toIntExact(sendInput(1)), 1); break;
                        case 3: processStatus(toIntExact(sendInput(4)), 4); break;
                        case 4: processStatus(toIntExact(sendInput(3)), 3); break;
                    }
                }
            });
            updateGUI(getGameBoard());
            mainArea.setEnabled(true);
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
            mainArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            mainArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            mainArea.setText(getGameBoard());
            mainArea.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}
                @Override
                public void keyPressed(KeyEvent e) {
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
                }
                @Override
                public void keyReleased(KeyEvent e) { }
            });
            mainComp.add(mainArea, BorderLayout.CENTER);
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
