package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DiningSimulation {
    private final int programmerCount;
    private final long totalPortions;
    private final int waiterCount;

    public DiningSimulation(int programmerCount, long totalPortions, int waiterCount) {
        this.programmerCount = programmerCount;
        this.totalPortions = totalPortions;
        this.waiterCount = waiterCount;
    }

    public SimulationResult run() throws InterruptedException {
        var spoons = new ArrayList<Spoon>();
        for (int i = 0; i < programmerCount; i++)
            spoons.add(new Spoon(i));

        var kitchen = new Kitchen(totalPortions, waiterCount);
        var startLatch = new CountDownLatch(programmerCount);

        var programmers = new ArrayList<Programmer>();
        for (int i = 0; i < programmerCount; i++) {
            Spoon left = spoons.get(i);
            Spoon right = spoons.get((i + 1) % programmerCount);
            programmers.add(new Programmer(i, left, right, kitchen, startLatch));
        }

        long start = System.currentTimeMillis();

        ExecutorService programmerPool = Executors.newFixedThreadPool(programmerCount);
        List<Future<?>> futures = new ArrayList<>();
        for (var p : programmers) {
            futures.add(programmerPool.submit(p));
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        programmerPool.shutdown();
        kitchen.shutdown();

        long duration = System.currentTimeMillis() - start;

        long[] eaten = programmers.stream().mapToLong(Programmer::getPortionsEatenCount).toArray();
        return new SimulationResult(eaten, duration);
    }
}
