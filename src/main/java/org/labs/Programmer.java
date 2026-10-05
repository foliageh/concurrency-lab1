package org.labs;

//import java.util.concurrent.locks.LockSupport;

import java.util.concurrent.CountDownLatch;

public class Programmer implements Runnable {
    private final int id;
    private final Spoon firstSpoon;
    private final Spoon secondSpoon;
    private final Kitchen kitchen;
    private final CountDownLatch startLatch;
    private long portionsEatenCount = 0;

    public Programmer(int id, Spoon leftSpoon, Spoon rightSpoon, Kitchen kitchen, CountDownLatch startLatch) {
        this.id = id;
        this.kitchen = kitchen;
        this.startLatch = startLatch;

        if (leftSpoon.getId() < rightSpoon.getId()) {
            this.firstSpoon = leftSpoon;
            this.secondSpoon = rightSpoon;
        } else {
            this.firstSpoon = rightSpoon;
            this.secondSpoon = leftSpoon;
        }
    }

    @Override
    public void run() {
        try {
            if (startLatch != null) {
                startLatch.countDown();
                startLatch.await();
            }

            while (!Thread.currentThread().isInterrupted()) {
                boolean gotSoup = kitchen.orderSoup(id, portionsEatenCount);
                if (!gotSoup) break;

                firstSpoon.pickUp();
                try {
                    secondSpoon.pickUp();
                    try {
                        eat();
                        portionsEatenCount++;
                    } finally {
                        secondSpoon.putDown();
                    }
                } finally {
                    firstSpoon.putDown();
                }

                think();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void eat() {
        //LockSupport.parkNanos(2L);
    }

    private void think() {
        //LockSupport.parkNanos(2L);
    }

    public long getPortionsEatenCount() {
        return portionsEatenCount;
    }
}
