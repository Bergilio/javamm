package pt.up.fe.comp2026.backend;

public class StackWorker {
    private int currentSize = 0;
    private int maxSize = 0;

    public void push() {
        currentSize++;
        if(currentSize > maxSize)
            maxSize = currentSize;
    }

    public void pop() {
        currentSize--;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
