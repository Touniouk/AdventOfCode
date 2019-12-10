package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static java.lang.Math.toIntExact;

public class Probleme9 {
    public static void main(String... args) throws IOException {
        IntcodeComputer computer = new IntcodeComputer(Arrays.stream(Files.lines(Paths.get("input9.txt"))
                .findFirst().get()
                .split(","))
                .mapToLong(Integer::parseInt).toArray());
        computer.compute();
        System.out.println(computer.getOutputQueue());
    }
}

class IntcodeComputer {
    private long[] memory;
    private long[] intcode;
    private int phaseSetting;
    private int position;
    private String op;
    private int relativeBase;
    private Queue<Integer> inputQueue;
    private Queue<Integer> outputQueue;

    IntcodeComputer(long[] memory) {
        this.memory = memory;
        resetIntcode();
        inputQueue = new ArrayDeque<>();
        outputQueue = new ArrayDeque<>();
    }

    IntcodeComputer(long[] memory, int phaseSetting) {
        this(memory);
        this.phaseSetting = phaseSetting;
    }

    private void resetIntcode() {
        intcode = Arrays.copyOf(memory, 500);
    }

    public void compute() {
        op = "000000" + intcode[0];
        String opcode = op.substring(op.length() - 2);
        String[] paramModes = op.substring(op.length()-5, op.length()-2).split("");
        int[] params = {0, 0, 0};
        int isPhaseSetting = 0;

        while (!opcode.equals("99")) {
            // Set parameters
            setParamOne(params, paramModes);
            if (!opcode.equals("03") && !opcode.equals("04") && !opcode.equals("09")) setParamTwo(params, paramModes);
            // Do operation
            switch (opcode) {
                case "01": add(params); break;
                case "02": multiply(params); break;
                case "03": input(params, isPhaseSetting); break;
                case "04": output(params); break;
                case "05": jumpIfTrue(params); break;
                case "06": jumpIfFalse(params); break;
                case "07": lessTan(params); break;
                case "08": equals(params); break;
                case "09": setRelativeBase(params);
            }
            opcode = op.substring(op.length() - 2);
            paramModes = op.substring(op.length()-5, op.length()-2).split("");
        }
    }

    private void setParamOne(int[] params, String[] paramModes) {
        switch (paramModes[2]) {
            case "1": params[0] = toIntExact(intcode[position+1]); break;
            case "2": params[0] = toIntExact(intcode[toIntExact(intcode[position+1]+relativeBase)]); break;
            case "0": params[0] = toIntExact(intcode[toIntExact(intcode[position+1])]);
        }
    }

    private void setParamTwo(int[] params, String[] paramModes) {
        switch (paramModes[1]) {
            case "1": params[1] = toIntExact(intcode[position+2]); break;
            case "2": params[1] = toIntExact(intcode[toIntExact(intcode[position+2]+relativeBase)]); break;
            case "0": params[1] = toIntExact(intcode[toIntExact(intcode[position+2])]);
        }
    }

    private void output(int[] params) {
        outputQueue.add(params[0]);
        increasePosition(2);
    }

    private void input(int[] params, int isPhaseSetting) {
        if (++isPhaseSetting == 1) intcode[toIntExact(intcode[position + 1])] = phaseSetting;
        else intcode[toIntExact(intcode[position + 1])] = inputQueue.poll();
        increasePosition(2);
    }

    private void add(int[] params) {
        intcode[toIntExact(intcode[position + 3])] = params[0] + params[1];
        increasePosition(4);
    }

    private void multiply(int[] params) { // 34_915_192
        intcode[toIntExact(intcode[position + 3])] = (long) params[0] * params[1];
        System.out.println((long) params[0] * params[1]);
        increasePosition(4);
    }

    private void jumpIfTrue(int[] params) {
        if (params[0] != 0) op = "000000" + intcode[(position = params[1])];
        else increasePosition(3);
    }

    private void jumpIfFalse(int[] params) {
        if (params[0] == 0) op = "000000" + intcode[(position = params[1])];
        else increasePosition(3);
    }

    private void lessTan(int[] params) {
        intcode[toIntExact(intcode[position + 3])] = params[0] < params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void equals(int[] params) {
        intcode[toIntExact(intcode[position + 3])] = params[0] == params[1] ? 1 : 0;
        increasePosition(4);
    }

    private void setRelativeBase(int[] params) {
        relativeBase += params[0];
        increasePosition(2);
    }

    private void increasePosition(int steps) {
        op = "000000" + intcode[(position += steps)];
    }

    public Queue<Integer> getOutputQueue() {
        return outputQueue;
    }
}
