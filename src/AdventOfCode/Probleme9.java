package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static java.lang.Math.toIntExact;

public class Probleme9 {
    public static void main(String... args) throws IOException {
        part1();
        part2();
    }

    private static void part1() throws IOException {
        IntcodeComputer computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray(), 1);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }

    private static void part2() throws IOException {
        IntcodeComputer computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get().split(",")).mapToLong(Long::parseLong).toArray(), 2);
        computer.compute();
        System.out.println(computer.getOutputQueue().poll());
    }
}

class IntcodeComputer {
    private long[] memory;
    private long[] intcode;
    private int phaseSetting;
    private int position;
    private int isPhaseSetting;
    private String op;
    private int relativeBase;
    private Queue<Long> inputQueue;
    private Queue<Long> outputQueue;

    IntcodeComputer(long[] memory) {
        this.memory = memory;
        resetIntcode();
        inputQueue = new ArrayDeque<>();
        outputQueue = new ArrayDeque<>();
        isPhaseSetting = 1;
    }

    IntcodeComputer(long[] memory, int phaseSetting) {
        this(memory);
        this.phaseSetting = phaseSetting;
        isPhaseSetting = 0;
    }

    private void resetIntcode() {
        intcode = Arrays.copyOf(memory, 10000);
    }

    void compute() {
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
                case "04": output(params);break;
                case "05": jumpIfTrue(params);break;
                case "06": jumpIfFalse(params);break;
                case "07": lessTan(params, paramModes[0]);break;
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

    private void input(String paramMode, int isPhaseSetting) {
        if (++isPhaseSetting == 1) {
            if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = phaseSetting;
            else intcode[toIntExact(intcode[position + 1] + relativeBase)] = phaseSetting;
        }
        else if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 1])] = inputQueue.poll();
        else intcode[toIntExact(intcode[position + 1] + relativeBase)] = inputQueue.poll();
        increasePosition(2);
    }

    private void output(long[] params) {
        outputQueue.add(params[0]);
        increasePosition(2);
    }

    private void add(long[] params, String paramMode) {
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] + params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] + params[1];
        increasePosition(4);
    }

    private void multiply(long[] params, String paramMode) {
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] * params[1];
        else intcode[toIntExact(intcode[position + 3] + relativeBase)] = params[0] * params[1];
        increasePosition(4);
    }

    private void jumpIfTrue(long[] params) {
        if (params[0] != 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void jumpIfFalse(long[] params) {
        if (params[0] == 0) op = "000000" + intcode[(position = toIntExact(params[1]))];
        else increasePosition(3);
    }

    private void lessTan(long[] params, String paramMode) {
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] < params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] < params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void equals(long[] params, String paramMode) {
        if (!paramMode.equals("2")) intcode[toIntExact(intcode[position + 3])] = params[0] == params[1] ? 1 : 0;
        else intcode[toIntExact(intcode[position + 3]) + relativeBase] = params[0] == params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void setRelativeBase(long[] params) {
        relativeBase += params[0];
        increasePosition(2);
    }

    private void increasePosition(int steps) {
        op = "000000" + intcode[(position += steps)];
    }

    Queue<Long> getOutputQueue() {
        return outputQueue;
    }
}
