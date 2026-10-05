package org.labs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dining Programmers Concurrency Tests")
class DiningSimulationTest {

    @Test
    @DisplayName("7 programmers, 1,000,000 portions, 2 waiters")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMainSimulation1MillionPortions() throws InterruptedException {
        int programmers = 7;
        long totalPortions = 1_000_000L;
        int waiters = 2;

        SimulationResult result = new DiningSimulation(programmers, totalPortions, waiters).run();

        long maxDelta = calculateMaxAllowedDelta(programmers, totalPortions, waiters);
        assertAll(
                () -> assertEquals(totalPortions, result.totalEaten(),
                        "Total eaten portions must match total prepared portions"),
                () -> assertTrue(result.maxEaten() - result.minEaten() <= maxDelta,
                        String.format("Delta between max (%d) and min (%d) should not exceed %d portions",
                                result.maxEaten(), result.minEaten(), maxDelta)),
                () -> assertTrue(result.minEaten() >= (totalPortions / programmers) - maxDelta,
                        "Each programmer must consume their fair share of portions")
        );
    }

    @RepeatedTest(value = 5, name = "Run {currentRepetition}/{totalRepetitions}: Small portion depletion boundary")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFoodExhaustionBoundary() throws InterruptedException {
        int programmers = 7;
        long totalPortions = 13L;
        int waiters = 3;

        SimulationResult result = new DiningSimulation(programmers, totalPortions, waiters).run();

        assertEquals(totalPortions, result.totalEaten(), "No portions should be over-allocated or lost. But "+result.totalEaten()+" > "+totalPortions);

        long servedCount = Arrays.stream(result.eatenPerProgrammer()).filter(eaten -> eaten > 0).count();
        assertTrue(servedCount >= programmers - waiters, "At least (programmers - waiters) must be served");
    }

    @ParameterizedTest(name = "Programmers: {0}, Portions: {1}, Waiters: {2}")
    @CsvSource({
            "2,  100000, 5",
            "20, 100000, 1",
            "10, 500000, 9",
            "3,  333333, 1"
    })
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testExtremeResourceRatios(int programmers, long portions, int waiters) throws InterruptedException {
        SimulationResult result = new DiningSimulation(programmers, portions, waiters).run();

        assertEquals(portions, result.totalEaten(), "Total consumed portions must equal initial kitchen stock");

        long maxDelta = calculateMaxAllowedDelta(programmers, portions, waiters);
        assertTrue(result.maxEaten() - result.minEaten() <= maxDelta,
                String.format("Delta between max (%d) and min (%d) should not exceed %d portions",
                        result.maxEaten(), result.minEaten(), maxDelta));
    }

    @Test
    @DisplayName("Kitchen Priority Unit Test: Most starved programmer served first")
    void testKitchenPriorityOrdering() throws Exception {
        OrderRequest fullProgrammer = new OrderRequest(1, 100);
        OrderRequest hungryProgrammer = new OrderRequest(2, 5);

        var queue = new PriorityBlockingQueue<OrderRequest>();
        queue.put(fullProgrammer);
        queue.put(hungryProgrammer);

        OrderRequest firstServed = queue.poll();
        assertNotNull(firstServed);
        assertEquals(2, firstServed.getProgrammerId(),
                "Waiters must serve the most starved programmer first");
    }

    @Test
    @DisplayName("Graceful shutdown and request rejection after kitchen closes")
    void testThreadCleanupAfterCompletion() throws InterruptedException {
        Kitchen kitchen = new Kitchen(10, 1);
        for (int i = 0; i < 10; i++) {
            kitchen.orderSoup(i % 2, i);
        }

        kitchen.shutdown();
        assertFalse(kitchen.orderSoup(1, 10),
                "Closed kitchen must immediately reject incoming orders");
    }

    private long calculateMaxAllowedDelta(int programmers, long totalPortions, int waiters) {
        long portionsPerProgrammer = totalPortions / programmers;
        return (waiters * 2L) + (portionsPerProgrammer / 1000L);
    }
}
