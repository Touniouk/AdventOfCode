package AdventOfCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.toIntExact;

public class Probleme13 {

    private final Map<Coords, String> gameBoardTiles = new HashMap<>();
    private GameGUI GUI;
    private Logger logger;
    private static long[] memory;

    int paddleXCoord;
    int ballXCoord;

    public static void main(String... args) throws IOException, InterruptedException {
        memory = Arrays.stream(Files.lines(Paths.get("input13.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
        Probleme13 p = new Probleme13();
        p.logger = new Logger(p, LogLevel.INFO);
//        p.part1();
        p.part2();
    }

    private void part1() throws InterruptedException {
        IntcodeComputer computer= new IntcodeComputer(memory, LogLevel.INFO);
        runComputer(computer);
        logger.log(LogLevel.INFO, "There are " +
                gameBoardTiles.entrySet().stream().filter(e -> e.getValue().equals("@")).count()
        + " block tiles");
    }

    private void part2() throws InterruptedException {
        long[] memory = Arrays.copyOf(Probleme13.memory, Probleme13.memory.length);
        memory[0] = 2;
        IntcodeComputer computer = new IntcodeComputer(memory, LogLevel.INFO);
        GUI = new GameGUI(computer, true);
        GUI.buildAndShowGUI();
        logger.log(LogLevel.INFO, "Final score: " + runComputer(computer));
        GUI.close();
    }

    private long runComputer(IntcodeComputer computer) throws InterruptedException {
        new Thread(computer).start();
        int[] input = new int[3];
        long score = 0;
        do {
            // Get all three outputs
            logger.log(LogLevel.DEBUG, "Waiting for output: " + computer.getOutputQueue());
            if ((input[0] = toIntExact(computer.getOutputQueue().take())) == computer.POISON) break;
            if ((input[1] = toIntExact(computer.getOutputQueue().take())) == computer.POISON) break;
            if ((input[2] = toIntExact(computer.getOutputQueue().take())) == computer.POISON) break;
            // Check if it's a score update
            if (input[0] == -1 && input[1] == 0) {
                logger.log(LogLevel.UNNECESSARY, "Score: " + score);
                score = input[2];
            }
            // Add the tile to the list
            else gameBoardTiles.put(new Coords(input[0], input[1]), getTile(input[2]));
            // Check if it's the ball or the paddle moving
            if (getTile(input[2]).equals("o")) ballXCoord = input[0];
            else if (getTile(input[2]).equals("_")) paddleXCoord = input[0];
            // Update the GUI
            if (GUI != null) GUI.updateGUI(getGameBoard(), String.valueOf(score));
        } while (computer.isRunning());
        return score;
    }

    private String getTile(int tileId) {
        switch (tileId) {
            case 0: return " ";
            case 1: return "#";
            case 2: return "$";
            case 3: return "_";
            case 4: return "o";
        }
        return String.valueOf(tileId);
    }

    private String getGameBoard() {
        // Get highest, lowest, most to the right and most to the left panels
        int lowest   = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.y)).get().y;
        int highest  = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.y)).get().y;
        int leftest  = gameBoardTiles.keySet().stream().min(Comparator.comparingInt(k -> k.x)).get().x;
        int rightest = gameBoardTiles.keySet().stream().max(Comparator.comparingInt(k -> k.x)).get().x;

        StringBuilder builder = new StringBuilder();
        String temp;
        for (int y = highest+1; y >= lowest-1; y--) {
            for (int x = leftest - 1; x <= rightest + 1; x++) {
                if ((temp = gameBoardTiles.get(new Coords(x, y))) != null) builder.append(temp);
            }
            builder.append("\n");
        }
        logger.log(LogLevel.RIDICULOUS, builder.toString());
        return builder.toString();
    }

    private class GameGUI {
        private int currentDirection = 0;
        private IntcodeComputer computer;
        private JFrame frame;
        private JTextArea textArea;
        private JLabel score;
        private boolean playWithUI;

        GameGUI(IntcodeComputer computer, boolean playWithUI) {
            this.computer = computer;
            this.playWithUI = playWithUI;
        }

        void updateKeyPressed() {
            try {
                if (computer.getInputQueue().take() == computer.POISON) return;
                if (!playWithUI) computer.getInputQueue().put((long) getCurrentDirection());
                else computer.getInputQueue().put((long) computeDirection());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        private int computeDirection() {
            return Integer.compare(ballXCoord, paddleXCoord);
        }

        private void buildAndShowGUI() {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationByPlatform(true);
            JPanel mainComp = new JPanel(new BorderLayout());
            score = new JLabel("0");
            textArea = new JTextArea(25, 43);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}
                @Override
                public void keyPressed(KeyEvent e) {
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_LEFT: currentDirection = -1; break;
                        case KeyEvent.VK_RIGHT : currentDirection = 1; break;
                        case KeyEvent.VK_UP: ;
                        case KeyEvent.VK_DOWN: currentDirection = 0; break;
                    }
                    updateKeyPressed();
                }
                @Override
                public void keyReleased(KeyEvent e) { }
            });
            mainComp.add(textArea, BorderLayout.CENTER);
            mainComp.add(score, BorderLayout.NORTH);
            frame.getContentPane().add(mainComp);
            frame.pack();
            frame.setVisible(true);
        }

        void updateGUI(String text, String score) {
            textArea.setText(text);
            this.score.setText(score);
            frame.validate();
        }

        int getCurrentDirection() {
           return currentDirection;
        }

        void close() {
            frame.dispose();
        }
    }
}

class Logger {
    private LogLevel logging;
    private Object caller;

    Logger(Object caller, LogLevel initialLogLevel) {
        this.caller = caller;
        logging = initialLogLevel;
    }

    void setLogLevel(LogLevel newLogLevel) { logging = newLogLevel; }

    void log(LogLevel messageLevel, String logMessage) {
        if (messageLevel.level <= logging.level)
            System.out.println("> " + caller.getClass().getSimpleName() + ": " + logMessage);
    }
}