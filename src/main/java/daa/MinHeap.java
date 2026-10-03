package daa;

public final class MinHeap {
    private static final int DEFAULT_CAPACITY = 16;

    private int[] a;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    public MinHeap(int initialCapacity) {
        a = new int[Math.max(1, initialCapacity)];
    }

    public static MinHeap buildHeap(int[] array) {
        MinHeap h = new MinHeap(array.length);
        for (int i = 0; i < array.length; i++) {
            h.a[i] = array[i];
            h.metrics.steps++;
            h.metrics.moves++;
        }
        h.size = array.length;
        for (int i = h.size / 2 - 1; i >= 0; i--) {
            h.metrics.steps++;
            h.siftDown(i, h.a[i]);
        }
        return h;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void insert(int x) {
        if (size == a.length) {
            grow();
        }
        int i = size;
        size++;
        while (i > 0) {
            int parent = (i - 1) >>> 1;
            metrics.steps++;
            metrics.comparisons++;
            if (a[parent] <= x) {
                break;
            }
            a[i] = a[parent];
            metrics.moves++;
            i = parent;
        }
        a[i] = x;
        metrics.moves++;
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.steps++;
        return a[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        int min = a[0];
        metrics.steps++;
        size--;
        if (size > 0) {
            int last = a[size];
            metrics.steps++;
            siftDown(0, last);
        }
        return min;
    }

    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = a[i];
        }
        return copy;
    }

    private void siftDown(int i, int v) {
        while (true) {
            int child = 2 * i + 1;
            if (child >= size) {
                break;
            }
            metrics.steps++;
            if (child + 1 < size) {
                metrics.steps++;
                metrics.comparisons++;
                if (a[child + 1] < a[child]) {
                    child++;
                }
            }
            metrics.comparisons++;
            if (v <= a[child]) {
                break;
            }
            a[i] = a[child];
            metrics.moves++;
            i = child;
        }
        a[i] = v;
        metrics.moves++;
    }

    private void grow() {
        int[] bigger = new int[a.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = a[i];
            metrics.steps++;
            metrics.moves++;
        }
        a = bigger;
    }
}