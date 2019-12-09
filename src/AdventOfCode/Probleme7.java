package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Probleme7 {
    public static void main(String args[]) throws IOException {
        int[] memory = Arrays.stream(Files.lines(Paths.get("input7.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();
        ArrayList<String> list = listAllPermutations("01234");
        int inputSignal = 0;
        int maxOutput = 0;
        String maxPhaseSetting = "";
        for (String perm : list) {
            for (char c : perm.toCharArray()) {
                inputSignal = runIntcode(Character.getNumericValue(c), inputSignal, Arrays.copyOf(memory, memory.length));
            }
            if (inputSignal > maxOutput) {
                maxPhaseSetting = perm;
                maxOutput = inputSignal;
            }
            inputSignal = 0;
        }
        System.out.println("Final thruster output is " + maxOutput + " with phase setting " + maxPhaseSetting);
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
    private static ArrayList<String> listAllPermutations(String s) {
        ArrayList<String> list = new ArrayList<>();
        listAllPermutations("", s, list);
        return list;
    }

    private static void listAllPermutations(String prefix, String s, ArrayList<String> list) {
        if (s.length() > 1) {
            for (int i = 0; i < s.length(); i++) {
                listAllPermutations(prefix + s.charAt(i), new StringBuilder(s).delete(i, i+1).toString(), list);
            }
        }
        else {
            list.add(prefix + s);
        }
    }
}