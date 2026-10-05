package org.labs;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Kitchen {
    private final AtomicLong remainingPortions;
    private final PriorityBlockingQueue<OrderRequest> orderQueue = new PriorityBlockingQueue<>();
    private final ExecutorService waiterPool;
    private volatile boolean isOpen = true;

    public Kitchen(long totalPortions, int waiterCount) {
        this.remainingPortions = new AtomicLong(totalPortions);
        this.waiterPool = Executors.newFixedThreadPool(waiterCount);
        for (int i = 0; i < waiterCount; i++) {
            waiterPool.submit(this::waiterLoop);
        }
    }

    public boolean orderSoup(int programmerId, long portionsEaten) throws InterruptedException {
        if (!isOpen && remainingPortions.get() <= 0) {
            return false;
        }

        var request = new OrderRequest(programmerId, portionsEaten);
        orderQueue.put(request);

        if (!isOpen) {
            request.getResponse().complete(false);
        }

        try {
            return request.getResponse().get();
        } catch (ExecutionException e) {
            return false;
        }
    }

    private void waiterLoop() {
        while (isOpen || !orderQueue.isEmpty()) {
            try {
                var request = orderQueue.poll(50, TimeUnit.MILLISECONDS);
                if (request == null) {
                    if (remainingPortions.get() <= 0) {
                        closeKitchen();
                        break;
                    }
                    continue;
                }

                long current = remainingPortions.get();
                if (current > 0 && remainingPortions.decrementAndGet() >= 0) {
                    request.getResponse().complete(true);
                } else {
                    request.getResponse().complete(false);
                    closeKitchen();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void closeKitchen() {
        this.isOpen = false;
        OrderRequest req;
        while ((req = orderQueue.poll()) != null) {
            req.getResponse().complete(false);
        }
    }

    public void shutdown() {
        closeKitchen();
        waiterPool.shutdownNow();
    }
}

