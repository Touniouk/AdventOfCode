package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.lang.Math.toIntExact;

public class Probleme8 {
    public static void main(String... args) throws IOException {
//        ArrayList<Map<String, Long>> test = Files.lines(Paths.get("input8.txt"))
//                .map(line -> Arrays.stream(line.split(""))
//                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
//                .collect(Collectors.toCollection(ArrayList::new));
//        Map<String, Long> min = test.stream().min( Comparator.comparingInt(o -> toIntExact(o.get("0")) )).get();
//        System.out.println(min);

        List<String> lines = Files.lines(Paths.get("input8.txt"))
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            for (String line :  lines) {
                char c = line.charAt(i);
                if (c != '2') {
                    if (c == '0') builder.append(" ");
                    else builder.append(c);
                    break;
                }
            }
        }
        for (int i = 0; i < builder.length()-1; i+=25) {
            System.out.println(builder.substring(i, i+25));
        }
    }
}

