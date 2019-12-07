package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Probleme4 {
    public static void main(String... args) throws IOException {
        int[] arr = Arrays.stream(
                Files.lines(Paths.get("input4.txt"))
                        .findFirst().get()
                        .split("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
        System.out.println(IntStream.rangeClosed(arr[0], arr[1]).filter(Probleme4::checksOut).count());
    }

    private static boolean checksOut(int i) {
        char[] arr = String.valueOf(i).toCharArray();
        for (int j = 1; j < arr.length; j++) if (arr[j-1] > arr[j]) return false;
        return Arrays.stream(String.valueOf(i).split(""))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .anyMatch(e -> e.getValue() == 2);
    }
}
