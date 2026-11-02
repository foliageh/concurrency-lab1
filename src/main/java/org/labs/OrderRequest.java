package org.labs;

import java.util.concurrent.CompletableFuture;

public class OrderRequest implements Comparable<OrderRequest> {
    private final int programmerId;
    private final long portionsEaten;
    private final CompletableFuture<Boolean> response = new CompletableFuture<>();

    public OrderRequest(int programmerId, long portionsEaten) {
        this.programmerId = programmerId;
        this.portionsEaten = portionsEaten;
    }

    public int getProgrammerId() {
        return programmerId;
    }

    public long getPortionsEaten() {
        return portionsEaten;
    }

    public CompletableFuture<Boolean> getResponse() {
        return response;
    }

    @Override
    public int compareTo(OrderRequest o) {
        return Long.compare(this.portionsEaten, o.portionsEaten);
    }
}
