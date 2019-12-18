package AdventOfCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Probleme14 {

    private Map<String, Ore> ores;
    private Queue<OreQuantity> oresNeeded = new ArrayDeque<>();
    private Map<Ore, Long> surplusOres = new HashMap<>();
    private long OREquantity = 0;

    public static void main(String... args) throws IOException {
        new Probleme14().start();
    }

    private void start() throws IOException {
//        LongStream.range(8129000, 8200000).parallel().forEach(i -> heavyOperation());
        // 1000000064693 - 8193615
        //  999999944536 - 8193614
        long FUELNeeded  = 8193615;
        processFileInput();
        ores.get("FUEL").toGetOre.get(0).reactionInput.forEach((k,v) -> oresNeeded.add(new OreQuantity(k, v*FUELNeeded)));

        while (!oresNeeded.isEmpty()) convertToRaw(oresNeeded.poll());
        System.out.println("Ore Quantity needed " + OREquantity);
        System.out.println("Ore Quantity dispon " + 1000000000000L);
    }

    private void convertToRaw(OreQuantity oreNeeded) {
        if (oreNeeded.ore.name.equals("ORE")) {
            OREquantity += oreNeeded.quantity;
//            System.out.println("OREQUANTITY " + OREquantity);
//            System.out.println("================================");
            return;
        }

        Reaction r = oreNeeded.ore.toGetOre.get(0);
        long quantGivenByReact = r.reactionProduce.get(oreNeeded.ore);
        long currentSurplus = surplusOres.get(oreNeeded.ore) == null ? 0 : surplusOres.get(oreNeeded.ore);
        long nbOfTimeToDoReact = 0;
        long totalAmountGenerated;
        while ((totalAmountGenerated = quantGivenByReact * nbOfTimeToDoReact + currentSurplus) < oreNeeded.quantity) {
            nbOfTimeToDoReact++;
        }
        long totalSurplus = totalAmountGenerated - oreNeeded.quantity;



//        System.out.println("We need " + oreNeeded.quantity + " " + oreNeeded.ore + " ores");
//        System.out.println(r + " gives us " + r.reactionProduce.get(oreNeeded.ore) + " " + oreNeeded.ore + " ores");
//        System.out.println("We need to do r " + nbOfTimeToDoReact + " times. " +
//                "This gives us " + nbOfTimeToDoReact + " * " + quantGivenByReact + " + " + currentSurplus + " = " + totalAmountGenerated +
//                " with " + totalSurplus + " surplus");



        surplusOres.put(oreNeeded.ore, totalSurplus);

        for (Map.Entry<Ore, Long> e : r.reactionInput.entrySet()) {
//            System.out.println("Adding " + nbOfTimeToDoReact*e.getValue() + " " + e.getKey().name + " ores");
            oresNeeded.add(new OreQuantity(e.getKey(), nbOfTimeToDoReact*e.getValue()));
        }

//        System.out.println("================================");
    }

    private void processFileInput() throws IOException {
        ores = new HashMap<>();
        Files.lines(Paths.get("input14.txt")).forEach(l -> {
            String[] arr = l.split(" => ");
            String input = arr[0];
            String[] produce = arr[1].split(" ");
            Reaction reaction = new Reaction();

            Arrays.stream(input.split(", ")).forEach(i -> {
                String[] inp = i.split(" ");
                ores.putIfAbsent(inp[1], new Ore(inp[1]));
                reaction.reactionInput.put(ores.get(inp[1]), Long.parseLong(inp[0]));
                ores.get(inp[1]).toUseOre.add(reaction);
            });

            ores.putIfAbsent(produce[1], new Ore(produce[1]));
            reaction.reactionProduce.put(ores.get(produce[1]), Long.parseLong(produce[0]));
            ores.get(produce[1]).toGetOre.add(reaction);
        });
    }

    class OreQuantity {
        Ore ore;
        long quantity;
        OreQuantity(Ore ore, long quantity) {
            this.ore = ore;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "{" + ore + "=" + quantity + "}";
        }
    }

    class Reaction {
        private Map<Ore, Long> reactionInput;
        private Map<Ore, Long> reactionProduce;

        Reaction() {
            reactionInput = new HashMap<>();
            reactionProduce = new HashMap<>();
        }

        @Override
        public String toString() {
            return reactionInput + " => " + reactionProduce;
        }
    }

    class Ore {
        String name;
        List<Reaction> toGetOre;
        List<Reaction> toUseOre;

        Ore(String name) {
            this.name = name;
            toGetOre = new ArrayList<>();
            toUseOre = new ArrayList<>();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
