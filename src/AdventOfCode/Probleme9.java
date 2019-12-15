package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static java.lang.Math.toIntExact;

public class Probleme9 {
    public static void main(String... args) throws IOException, InterruptedException {
        part1();
        part2();
    }

    private static void part1() throws IOException, InterruptedException {
        IntcodeComputer computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray(), null, 1, false);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }

    private static void part2() throws IOException, InterruptedException {
        IntcodeComputer computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray(), null, 2, false);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }
}

class IntcodeComputer implements Runnable {
    private long[] memory;
    private long[] intcode;
    private int phaseSetting;
    private int position;
    private int isPhaseSetting;
    private String op;
    private int relativeBase;
    private Queue<Long> inputQueue;
    private Queue<Long> outputQueue;
    private Operator operator;
    private boolean logging;

    IntcodeComputer(long[] memory, Operator operator, boolean logging) {
        this.memory = memory;
        resetIntcode();
        inputQueue = new ArrayDeque<>();
        outputQueue = new ArrayDeque<>();
        isPhaseSetting = 1;
        this.logging = logging;
        this.operator = operator;
    }

    IntcodeComputer(long[] memory, Operator operator, int phaseSetting, boolean logging) {
        this(memory, operator, logging);
        this.phaseSetting = phaseSetting;
        isPhaseSetting = 0;
    }

    private void resetIntcode() {
        intcode = Arrays.copyOf(memory, 10000);
    }

    @Override
    public void run() {
        try {
            compute();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            log("Input = " + phaseSetting);
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = phaseSetting;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = phaseSetting;
        } else {
            while (inputQueue.isEmpty()) waitForSignal();
            log("Input = " + inputQueue.peek());
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = inputQueue.poll();
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = inputQueue.poll();
        }
        increasePosition(2);
    }

    private void output(long output) {
        log("Output = " + output);
        outputQueue.add(output);
        if (operator != null) operator.nudge();
        increasePosition(2);
    }

    private void add(long[] params, String paramMode) {
        log("Add");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] + params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] + params[1];
        increasePosition(4);
    }

    private void multiply(long[] params, String paramMode) {
        log("Multiply");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] * params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] * params[1];
        increasePosition(4);
    }

    private void jumpIfTrue(long[] params) {
        log("Jump if true");
        if (params[0] != 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void jumpIfFalse(long[] params) {
        log("Jump if false");
        if (params[0] == 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void lessThan(long[] params, String paramMode) {
        log("Less than");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] < params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] < params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void equals(long[] params, String paramMode) {
        log("Equals");
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] == params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] == params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void setRelativeBase(long[] params) {
        log("Set relative base");
        relativeBase += params[0];
        increasePosition(2);
    }

    private void increasePosition(int steps) {
        op = "000000" + intcode[(position += steps)];
    }

    public synchronized void addInput(long input) {
        inputQueue.add(input);
        notify();
    }

    private synchronized void waitForSignal() {
        try { wait(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void log(String logMessage) {
        if (logging) System.out.println("> Computer: " + logMessage);
    }

    Queue<Long> getOutputQueue() {
        return outputQueue;
    }
}

interface Operator {
    void nudge();
}