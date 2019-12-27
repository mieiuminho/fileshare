package util;

public interface RequestsBuffer<E> {
    void add(E v) throws InterruptedException;
    E get() throws InterruptedException;
}
