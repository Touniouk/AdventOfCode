package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Probleme2 {
    public static void main(String args[]) throws IOException {
        int[] memory = Arrays.stream(Files.lines(Paths.get("input2.txt")).findFirst().get()
                .split(","))
                .mapToInt(Integer::parseInt).toArray();
        int noun, verb;

        for (noun = 0; noun < 100; noun++) for (verb = 0; verb < 100; verb++) {
            int[] intcode = Arrays.copyOf(memory, memory.length);
            intcode[1] = noun;
            intcode[2] = verb;
            int operation = intcode[0], position = 0;
            while (operation != 99) {
                intcode[intcode[position + 3]] = (operation == 1) ?
                        intcode[intcode[position + 1]] + intcode[intcode[position + 2]] :
                        intcode[intcode[position + 1]] * intcode[intcode[position + 2]];
                operation = intcode[(position += 4)];
            }
            if (intcode[0] == 19690720) {
                System.out.println("noun = " + noun + ", verb = " + verb + ", result = " + (100 * noun + verb));
                break;
            }
        }
    }
}