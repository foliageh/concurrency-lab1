package org.labs;

import java.util.concurrent.locks.ReentrantLock;

public class Spoon {
    private final int id;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Spoon(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void pickUp() {
        lock.lock();
    }

    public void putDown() {
        lock.unlock();
    }
}
