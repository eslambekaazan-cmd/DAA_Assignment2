package daa;

public final class DynamicArray implements IntSequence {
    private static final int DEFAULT_CAPACITY = 10;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = 1;
        }
        data = new int[initialCapacity];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    @Override
    public void add(int x) {
        ensureCapacityForOneMore();
        data[size++] = x;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        ensureCapacityForOneMore();
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            metrics.steps++;
            metrics.moves++;
        }
        data[index] = x;
        size++;
    }

    @Override
    public int remove(int index) {
        checkElementIndex(index);
        int removed = data[index];
        metrics.steps++;
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            metrics.steps++;
            metrics.moves++;
        }
        size--;
        return removed;
    }

    @Override
    public int get(int index) {
        checkElementIndex(index);
        metrics.steps++;
        return data[index];
    }

    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.steps++;
            metrics.comparisons++;
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
    }

    private void ensureCapacityForOneMore() {
        if (size == data.length) {
            int[] bigger = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = data[i];
                metrics.steps++;
                metrics.moves++;
            }
            data = bigger;
        }
    }
}