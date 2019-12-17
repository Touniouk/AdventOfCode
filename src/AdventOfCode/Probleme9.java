package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.Math.toIntExact;

public class Probleme9 {

    private static long[] memory;

    public static void main(String... args) throws IOException, InterruptedException {
        memory = Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray();
        part1();
        part2();
    }

    private static void part1() throws InterruptedException {
        // TODO: Don't use poll()
        IntcodeComputer computer = new IntcodeComputer(memory, 1, LoggingLevel.QUIET);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }

    private static void part2() throws InterruptedException {
        // TODO: Don't use poll()
        IntcodeComputer computer = new IntcodeComputer(memory, 2, LoggingLevel.QUIET);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }
}

class IntcodeComputer implements Runnable {
    final long POISON = -123_456_789;

    private final long[] memory;
    private long[] intcode;
    private int phaseSetting;
    private int position;
    private int isPhaseSetting;
    private String op;
    private int relativeBase;
    private final BlockingQueue<Long> inputQueue;
    private final BlockingQueue<Long> outputQueue;
    private boolean isRunning = false;
    private LoggingLevel logging;

    IntcodeComputer(long[] memory, LoggingLevel logging) {
        this.memory = memory;
        resetIntcode();
        inputQueue = new ArrayBlockingQueue<>(1);
        outputQueue = new LinkedBlockingDeque<>();
        isPhaseSetting = 1;
        this.logging = logging;
    }

    IntcodeComputer(long[] memory, int phaseSetting, LoggingLevel logging) {
        this(memory, logging);
        this.phaseSetting = phaseSetting;
        isPhaseSetting = 0;
    }

    private void resetIntcode() {
        intcode = Arrays.copyOf(memory, 10000);
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            compute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
            outputQueue.add(POISON);
        }
    }

    void compute() throws InterruptedException {
        op = "000000" + intcode[0];
        String opcode = op.substring(op.length() - 2);
        String[] paramModes = op.substring(op.length() - 5, op.length() - 2).split("");
        long[] params = {0, 0, 0};

        while (!opcode.equals("99")) {
            // Set parameters
            setParamOne(params, paramModes);
            if (!opcode.equals("03") && !opcode.equals("04") && !opcode.equals("09")) setParamTwo(params, paramModes);
            // Do operation
            switch (opcode) {
                case "01": add(params, paramModes[0]);break;
                case "02": multiply(params, paramModes[0]);break;
                case "03": input(paramModes[2], isPhaseSetting);break;
                case "04": output(params[0]);break;
                case "05": jumpIfTrue(params);break;
                case "06": jumpIfFalse(params);break;
                case "07": lessThan(params, paramModes[0]);break;
                case "08": equals(params, paramModes[0]);break;
                case "09": setRelativeBase(params);
            }
            opcode = op.substring(op.length() - 2);
            paramModes = op.substring(op.length() - 5, op.length() - 2).split("");
        }
        log(LoggingLevel.INFO, "Finished Computing");
    }

    private void setParamOne(long[] params, String[] paramModes) {
         switch (paramModes[2]) {
            case "1": params[0] = intcode[position+1]; break;
            case "2": params[0] = intcode[toIntExact(intcode[position+1]+relativeBase)]; break;
            case "0": params[0] = intcode[toIntExact(intcode[position+1])];
        }
    }

    private void setParamTwo(long[] params, String[] paramModes) {
        switch (paramModes[1]) {
            case "1": params[1] = intcode[position+2]; break;
            case "2": params[1] = intcode[toIntExact(intcode[position+2]+relativeBase)]; break;
            case "0": params[1] = intcode[toIntExact(intcode[position+2])];
        }
    }

    private void input(String paramMode, int isPhaseSetting) throws InterruptedException {
        if (++isPhaseSetting == 1) {
            log(LoggingLevel.IO, "Input = " + phaseSetting);
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = phaseSetting;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = phaseSetting;
        } else {
            log(LoggingLevel.IO, "Waiting for input");
            long input = inputQueue.take();
            log(LoggingLevel.IO, "Input = " + input);
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = input;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = input;
        }
        increasePosition(2);
    }

    private void output(long output) throws InterruptedException {
        log(LoggingLevel.IO, "Output = " + output);
        outputQueue.put(output);
        increasePosition(2);
    }

    private void add(long[] params, String paramMode) {
        log(LoggingLevel.DEBUG, "Add");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] + params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] + params[1];
        increasePosition(4);
    }

    private void multiply(long[] params, String paramMode) {
        log(LoggingLevel.DEBUG, "Multiply");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] * params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] * params[1];
        increasePosition(4);
    }

    private void jumpIfTrue(long[] params) {
        log(LoggingLevel.DEBUG, "Jump if true: " + params[0]);
        if (params[0] != 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void jumpIfFalse(long[] params) {
        log(LoggingLevel.DEBUG, "Jump if false: " + params[0]);
        if (params[0] == 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void lessThan(long[] params, String paramMode) {
        log(LoggingLevel.DEBUG, "Less than");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] < params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] < params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void equals(long[] params, String paramMode) {
        log(LoggingLevel.DEBUG, "Equals");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] == params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] == params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void setRelativeBase(long[] params) {
        log(LoggingLevel.DEBUG, "Set relative base to " + (relativeBase+params[0]));
        relativeBase += params[0];
        increasePosition(2);
    }

    private void increasePosition(int steps) {
        op = "000000" + intcode[(position += steps)];
    }

    private void log(LoggingLevel messageLevel, String logMessage) {
        if (messageLevel.level <= logging.level) System.out.println("> Computer: " + logMessage);
    }

    BlockingQueue<Long> getOutputQueue() {
        return outputQueue;
    }

    BlockingQueue<Long> getInputQueue() {
        return inputQueue;
    }

    boolean isRunning() {
        return isRunning;
    }
}

enum LoggingLevel {
    DEBUG(3), IO(2), INFO(1), QUIET(0);

    public final int level;

    LoggingLevel(int level) {
        this.level = level;
    }
}