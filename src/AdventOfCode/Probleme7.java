package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Probleme7 {
    public static void main(String args[]) throws IOException {
        int[] memory = Arrays.stream(Files.lines(Paths.get("input7.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();

        int[] phaseSettings = {4,3,2,1,0};
        int inputSignal = 0;
        for (int phaseSetting : phaseSettings) {
            System.out.print("Running with phaseSetting " + phaseSetting + " and input " + inputSignal + ". ");
            inputSignal = runIntcode(phaseSetting, inputSignal, Arrays.copyOf(memory, memory.length));
        }
        System.out.println("Final thruster output is " + inputSignal);
    }

    public static int runIntcode(int phaseSetting, int inputSignal, int[] intcode) {
        String operation = "000000" + intcode[0];
        int position = 0, input = 0;
        String opcode = operation.substring(operation.length()-2);
        String[] paramModes = operation.substring(operation.length()-5, operation.length()-2).split("");
        int[] params = {0,0,0};

        while (!opcode.equals("99")) {
            try {
                params[0] = paramModes[2].equals("1") ? intcode[position + 1] : intcode[intcode[position + 1]];
                params[1] = paramModes[1].equals("1") ? intcode[position + 2] : intcode[intcode[position + 2]];
            } catch (ArrayIndexOutOfBoundsException ex) {}
            switch (opcode) {
                case "01":
                    intcode[intcode[position+3]] = params[0] + params[1];
                    operation = "000000" + intcode[(position += 4)];
                    break;
                case "02":
                    intcode[intcode[position+3]] = params[0] * params[1];
                    operation = "000000" + intcode[(position += 4)];
                    break;
                case "03":
                    intcode[intcode[position + 1]] = input == 0 ? phaseSetting : inputSignal;
                    input++;
                    operation = "000000" + intcode[(position += 2)];
                    break;
                case "04":
                    System.out.println("Output: " + params[0]);
                    return params[0];
                case "05":
                    if (params[0] != 0) operation = "000000" + intcode[(position = params[1])];
                    else operation = "000000" + intcode[(position += 3)];
                    break;
                case "06":
                    if (params[0] == 0) operation = "000000" + intcode[(position = params[1])];
                    else operation = "000000" + intcode[(position += 3)];
                    break;
                case "07":
                    intcode[intcode[position+3]] = params[0] < params[1] ? 1 : 0;
                    operation = "000000" + intcode[(position += 4)];
                    break;
                case "08":
                    intcode[intcode[position+3]] = params[0] == params[1] ? 1 : 0;
                    operation = "000000" + intcode[(position += 4)];
                    break;
            }
            opcode = operation.substring(operation.length()-2);
            paramModes = operation.substring(operation.length()-5, operation.length()-2).split("");
        }
        System.out.println("HERE");
        return -1;
    }
}