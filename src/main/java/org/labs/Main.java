package org.labs;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int programmerCount = 7;
        long totalPortions = 1_000_000L;
        int waiterCount = 2;

        DiningSimulation simulation = new DiningSimulation(programmerCount, totalPortions, waiterCount);
        SimulationResult result = simulation.run();
        result.printReport(programmerCount, waiterCount, totalPortions);
    }
}
