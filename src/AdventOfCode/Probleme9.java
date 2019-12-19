package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;

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
        IntcodeComputer computer = new IntcodeComputer(memory, 1, LogLevel.QUIET);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }

    private static void part2() throws InterruptedException {
        // TODO: Don't use poll()
        IntcodeComputer computer = new IntcodeComputer(memory, 2, LogLevel.QUIET);
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
    private Logger logger;

    IntcodeComputer(long[] memory, LogLevel logging) {
        this.memory = memory;
        resetIntcode();
        inputQueue = new SynchronousQueue<>();
        outputQueue = new LinkedBlockingDeque<>();
        isPhaseSetting = 1;
        logger = new Logger(this, logging);
    }

    IntcodeComputer(long[] memory, int phaseSetting, LogLevel logging) {
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
            logger.log(LogLevel.IO, "Outputting POISON");
            try {
                inputQueue.offer(POISON);
                outputQueue.put(POISON);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.log(LogLevel.DEBUG, "Done running");
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
        logger.log(LogLevel.INFO, "Finished Computing");
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
            logger.log(LogLevel.IO, "Input = " + phaseSetting);
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = phaseSetting;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = phaseSetting;
        } else {
            logger.log(LogLevel.IO, "Waiting for input");
            inputQueue.put(1L); // Throwaway to show the Producer we want a value
            long input = inputQueue.take();
            logger.log(LogLevel.IO, "Input = " + input);
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = input;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = input;
        }
        increasePosition(2);
    }

    private void output(long output) throws InterruptedException {
        logger.log(LogLevel.IO, "Output = " + output);
        outputQueue.put(output);
        increasePosition(2);
    }

    private void add(long[] params, String paramMode) {
        logger.log(LogLevel.DEBUG, "Add");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] + params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] + params[1];
        increasePosition(4);
    }

    private void multiply(long[] params, String paramMode) {
        logger.log(LogLevel.DEBUG, "Multiply");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] * params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] * params[1];
        increasePosition(4);
    }

    private void jumpIfTrue(long[] params) {
        logger.log(LogLevel.DEBUG, "Jump if true: " + params[0]);
        if (params[0] != 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void jumpIfFalse(long[] params) {
        logger.log(LogLevel.DEBUG, "Jump if false: " + params[0]);
        if (params[0] == 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void lessThan(long[] params, String paramMode) {
        logger.log(LogLevel.DEBUG, "Less than");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] < params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] < params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void equals(long[] params, String paramMode) {
        logger.log(LogLevel.DEBUG, "Equals");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] == params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] == params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void setRelativeBase(long[] params) {
        logger.log(LogLevel.DEBUG, "Set relative base to " + (relativeBase+params[0]));
        relativeBase += params[0];
        increasePosition(2);
    }

    private void increasePosition(int steps) {
        op = "000000" + intcode[(position += steps)];
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