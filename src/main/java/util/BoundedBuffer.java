package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The BoundedBuffer is a concurrent Queue implemented in FIFO with limited size.
 *
 * @param <E> the type of elements in this queue
 */
public class BoundedBuffer<E> {
    private int max;
    private List<E> buffer;
    private Lock lock;
    private Condition notFull;
    private Condition notEmpty;

    public BoundedBuffer(final int size) {
        this.max = size;
        this.buffer = new ArrayList<>(size);
        this.lock = new ReentrantLock();
        this.notFull = this.lock.newCondition();
        this.notEmpty = this.lock.newCondition();
    }

    /**
     * Inserts a element in the end of this buffer.
     *
     * @param v The element to insert.
     * @throws InterruptedException When a thread interrupted.
     */
    public void add(final E v) throws InterruptedException {
        this.lock.lock();
        try {
            while (this.buffer.size() == this.max) {
                this.notFull.await();
            }
            this.buffer.add(v);
            this.notEmpty.signal();

        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Returns and removes the first element in this buffer.
     *
     * @return The first element of the buffer.
     * @throws InterruptedException When a thread interrupted.
     */
    public E get() throws InterruptedException {
        this.lock.lock();
        try {
            while (this.buffer.size() == 0) {
                this.notEmpty.await();
            }
            E ret = this.buffer.remove(0);
            this.notFull.signal();
            return ret;
        } finally {
            this.lock.unlock();
        }
    }
}
