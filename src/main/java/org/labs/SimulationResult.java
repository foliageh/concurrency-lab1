package org.labs;

import java.util.Arrays;

public record SimulationResult(long[] eatenPerProgrammer, long durationMillis) {
    public long totalEaten() {
        return Arrays.stream(eatenPerProgrammer).sum();
    }

    public long minEaten() {
        return Arrays.stream(eatenPerProgrammer).min().orElse(0);
    }

    public long maxEaten() {
        return Arrays.stream(eatenPerProgrammer).max().orElse(0);
    }

    public boolean isFair(long maxAllowedLead) {
        return (maxEaten() - minEaten()) <= maxAllowedLead;
    }

    public void printReport(int programmerCount, int waiterCount, long totalPortions) {
        System.out.println("DINING SIMULATION REPORT:");
        System.out.println("Programmers: " + programmerCount);
        System.out.println("Waiters: " + waiterCount);
        System.out.println("Total Portions Consumed: " + totalEaten() + "/" + totalPortions);
        System.out.println("Execution Time: " + durationMillis + " ms");
        for (int i = 0; i < eatenPerProgrammer.length; i++)
            System.out.printf("  Programmer %d consumed: %d portions%n", i, eatenPerProgrammer[i]);
        System.out.println("Portion Delta (Max - Min): " + (maxEaten() - minEaten()) + " portions");
    }
}