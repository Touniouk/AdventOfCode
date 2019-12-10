package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class Probleme7 {
    static int[] memory;

    public static void main(String args[]) throws IOException, InterruptedException, ExecutionException {
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

    private void part2() throws IOException, InterruptedException, ExecutionException {
        memory = Arrays.stream(Files.lines(Paths.get("input7.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();
        ArrayList<String> list = listAllPermutations("56789");

        int max = 0;
        String sequence = "";
        for (String s : list) {
            int thisPerm = tryPermutation(Arrays.stream(s.split("")).mapToInt(Integer::parseInt).toArray());
            if (thisPerm > max) {
                max = thisPerm;
                sequence = s;
            }
        }
        System.out.println("Max = " + max + ", sequence = " + sequence);
    }

    private static int tryPermutation(int[] phaseSettings) throws InterruptedException, ExecutionException {
        Amplifier ampE = new Amplifier("amp E", memory, phaseSettings[4]);
        Amplifier ampD = new Amplifier("amp D", memory, phaseSettings[3], ampE);
        Amplifier ampC = new Amplifier("amp C", memory, phaseSettings[2], ampD);
        Amplifier ampB = new Amplifier("amp B", memory, phaseSettings[1], ampC);
        Amplifier ampA = new Amplifier("amp A", memory, phaseSettings[0], ampB);
        ampE.setAfter(ampA);
        ampA.passInput(0);

        List<Callable<Integer>> amps = Arrays.asList(ampA, ampB, ampC, ampD, ampE);
        ExecutorService executor = Executors.newFixedThreadPool(amps.size());
        List<Future<Integer>> futures = executor.invokeAll(amps);
        int finalOutput = futures.get(4).get();

        System.out.println("Phase settings " + Arrays.toString(phaseSettings) + " output " + finalOutput);
        return finalOutput;
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

class Amplifier implements Callable<Integer> {
    Amplifier after;
    String name;
    Queue<Integer> inputQueue;
    int[] memory;
    int[] intcode;
    int phaseSetting;
    boolean running = false;

    public Amplifier(String name, int[] memory, int phaseSetting, Amplifier after) {
        this(name, memory, phaseSetting);
        this.after = after;
    }

    public Amplifier(String name, int[] memory, int phaseSetting) {
        this.name= name;
        this.memory = memory;
        this.phaseSetting = phaseSetting;
        intcode = Arrays.copyOf(memory, memory.length);
        inputQueue = new PriorityQueue<>();
    }

    @Override
    public Integer call() throws InterruptedException {
//        System.out.println(name + " started");
        running = true;
        Thread.sleep(50);

        // Setup
        String operation = "000000" + intcode[0];
        int position = 0, input = 0;
        String opcode = operation.substring(operation.length()-2);
        String[] paramModes = operation.substring(operation.length()-5, operation.length()-2).split("");
        int[] params = {0,0,0};
        int finalOutput = 0;

        // Start
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
                    if (input == 0) {
                        intcode[intcode[position + 1]] = phaseSetting;
                        input++;
                    } else {
//                        System.out.println(name + " is waiting for input");
                        if (inputQueue.isEmpty()) waitForSignal(); // Waiting for input
//                        System.out.println(name + " received input " + inputQueue.peek());
                        intcode[intcode[position + 1]] = inputQueue.poll();
                    }
                    operation = "000000" + intcode[(position += 2)];
                    break;
                case "04":
//                    System.out.println(name + " calling thread " + after.name + " with input " + params[0]);
                    if (after.running) after.passInput(params[0]);
                    else finalOutput = params[0];
                    operation = "000000" + intcode[(position += 2)];
                    break;
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
//        System.out.println(name + " done");
        running = false;
        return finalOutput;
    }

    private synchronized void waitForSignal() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void passInput(int input) {
        inputQueue.add(input);
        notify();
    }

    public void setAfter(Amplifier after) {
        this.after = after;
    }
}