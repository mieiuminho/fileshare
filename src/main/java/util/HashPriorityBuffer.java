package util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class HashPriorityBuffer implements RequestsBuffer<String> {
    private int max;
    private Map<String, MutableInt> priorities;
    private Queue<String> buffer;
    private Lock lock;
    private Condition notFull;
    private Condition notEmpty;

    public final class MutableInt implements Comparable<MutableInt> {
        private int value = 1;

        public int getValue() {
            return this.value;
        }

        public void increment() {
            this.value++;
        }

        public void decrement() {
            this.value--;
        }

        @Override
        public int compareTo(final MutableInt other) {
            if (value == 1) {
                return value - other.getValue() + 1;
            } else {
                return value - other.getValue();
            }
        }
    }

    public HashPriorityBuffer(final int size) {
        this.max = size;
        this.priorities = new HashMap<>();
        this.buffer = new PriorityQueue<>(size, Comparator.comparing(r -> this.priorities.get(getSessionID(r))));
        this.lock = new ReentrantLock();
        this.notFull = this.lock.newCondition();
        this.notEmpty = this.lock.newCondition();
    }

    public void add(final String request) throws InterruptedException {
        this.lock.lock();
        try {
            while (this.buffer.size() == this.max) {
                this.notFull.await();
            }

            String requestID = getSessionID(request);
            MutableInt count = this.priorities.get(requestID);
            if (count == null) {
                this.priorities.put(requestID, new MutableInt());
            } else {
                count.increment();
            }

            this.buffer.add(request);
            this.notEmpty.signal();
        } finally {
            this.lock.unlock();
        }
    }

    public String get() throws InterruptedException {
        this.lock.lock();
        try {
            while (this.buffer.size() == 0) {
                this.notEmpty.await();
            }

            String request = this.buffer.poll();
            String requestID = getSessionID(request);
            MutableInt count = this.priorities.get(requestID);
            if (count.getValue() == 1) {
                this.priorities.remove(requestID);
            } else {
                count.decrement();
            }

            this.notFull.signal();
            return request;
        } finally {
            this.lock.unlock();
        }
    }

    public static String getSessionID(final String request) {
        return request.split("\\s+")[0];
    }
}
