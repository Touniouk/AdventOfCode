package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Probleme7 {
    public static void main(String args[]) throws IOException {
//        part1();
        new Probleme7().part2();
    }

    private static void part1() throws IOException {
        int[] memory = Arrays.stream(Files.lines(Paths.get("input7.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();
        ArrayList<String> list = listAllPermutations("01234");
        int inputSignal = 0;
        int maxOutput = 0;
        String maxPhaseSetting = "";
        for (String perm : list) {
            for (char c : perm.toCharArray()) {
//                inputSignal = runIntcode(Character.getNumericValue(c), inputSignal, Arrays.copyOf(memory, memory.length));
            }
            if (inputSignal > maxOutput) {
                maxPhaseSetting = perm;
                maxOutput = inputSignal;
            }
            inputSignal = 0;
        }
        System.out.println("Final thruster output is " + maxOutput + " with phase setting " + maxPhaseSetting);
    }

    private void part2() throws IOException {
        int[] memory = Arrays.stream(Files.lines(Paths.get("input7.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();
        ArrayList<String> list = listAllPermutations("56789");

        int[] phaseSettings = {5,6,7,8,9};

        Amplifier ampE = new Amplifier("amp E", memory, phaseSettings[0]);
        Amplifier ampD = new Amplifier("amp D", memory, phaseSettings[1], ampE);
        Amplifier ampC = new Amplifier("amp C", memory, phaseSettings[2], ampD);
        Amplifier ampB = new Amplifier("amp B", memory, phaseSettings[3], ampC);
        Amplifier ampA = new Amplifier("amp A", memory, phaseSettings[4], ampB);
        ampE.setAfter(ampA);

        Thread firstThread = new Thread(ampA);
        firstThread.start();
        new Thread(ampB).start();
        new Thread(ampC).start();
        new Thread(ampD).start();
        new Thread(ampE).start();
        ampA.passInput(0);
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
        else list.add(prefix + s);
    }
}

class Amplifier implements Runnable {
    Amplifier after;
    String name;
    int input;
    int[] memory;
    int[] intcode;
    int phaseSetting;

    public Amplifier(String name, int[] memory, int phaseSetting, Amplifier after) {
        this(name, memory, phaseSetting);
        this.after = after;
    }

    public Amplifier(String name, int[] memory, int phaseSetting) {
        this.name= name;
        this.memory = memory;
        this.phaseSetting = phaseSetting;
        intcode = Arrays.copyOf(memory, memory.length);
        input = 0;
    }

    @Override
    public void run() {
        System.out.println(name + " started and is waiting");
        waitForInput();
        System.out.println(name + " received input " + input);
        runIntcode(input);
        System.out.println(name + " calling thread " + after.name + " with input 3");
        after.passInput(90);
        System.out.println(name + " done");
    }

    private synchronized void waitForInput() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runIntcode(int inputSignal) {
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
                    input = params[0];
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
    }

    public synchronized void passInput(int input) {
        this.input = input;
        notify();
    }

    public void setAfter(Amplifier after) {
        this.after = after;
    }
}