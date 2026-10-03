package daa;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

public final class Benchmark {
    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final long SEED = 42;
    static final int WARMUP_RUNS = 3;
    static final int MEASURED_RUNS = 5;
    static final int GET_QUERIES = 10_000;
    static final int SEARCH_QUERIES = 1_000;
    static final int UPDATES = 1_000;
    static final int VALUE_BOUND = 1_000_000;

    static volatile long sink;

    private record Result(long nanos, long steps, long moves, long comparisons) {
    }

    private interface Trial {
        Result run();
    }

    public static void main(String[] args) throws IOException {
        Path out = Paths.get(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(out))) {
            csv.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");
            for (int n : SIZES) {
                System.out.println("n = " + n);
                both(csv, "W1", "-", n, (s) -> () -> w1(s.get(), n));
                both(csv, "W2", "-", n, (s) -> () -> w2(s.get(), n));
                both(csv, "W3", "head", n, (s) -> () -> w3(s.get(), n, 0));
                both(csv, "W3", "middle", n, (s) -> () -> w3(s.get(), n, n / 2));
                measure(csv, "W4", "-", "MinHeap", n, () -> w4(n));
                measure(csv, "W5", "-", "MinHeap-buildHeap", n, () -> w5Build(n));
                measure(csv, "W5", "-", "MinHeap-insertN", n, () -> w5Insert(n));
            }
        }
        System.out.println("written " + out);
    }

    private interface TrialFactory {
        Trial create(Supplier<IntSequence> structure);
    }

    private static void both(PrintWriter csv, String workload, String variant, int n, TrialFactory f) {
        measure(csv, workload, variant, "DynamicArray", n, f.create(DynamicArray::new));
        measure(csv, workload, variant, "MyLinkedList", n, f.create(MyLinkedList::new));
    }

    private static void measure(PrintWriter csv, String workload, String variant, String structure, int n, Trial t) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            t.run();
        }
        long[] times = new long[MEASURED_RUNS];
        Result last = null;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            last = t.run();
            times[i] = last.nanos();
        }
        sortSmall(times);
        double medianMs = times[MEASURED_RUNS / 2] / 1_000_000.0;
        csv.printf(Locale.ROOT, "%s,%s,%s,%d,%.4f,%d,%d,%d%n", workload, variant, structure, n,
                medianMs, last.steps(), last.moves(), last.comparisons());
        csv.flush();
        System.out.printf(Locale.ROOT, "  %s %-7s %-18s %10.4f ms  steps=%d moves=%d cmp=%d%n",
                workload, variant, structure, medianMs, last.steps(), last.moves(), last.comparisons());
    }

    private static void sortSmall(long[] a) {
        for (int i = 1; i < a.length; i++) {
            long v = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > v) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = v;
        }
    }

    private static int[] fill(IntSequence s, int n, Random rnd) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(VALUE_BOUND);
            s.add(values[i]);
        }
        return values;
    }

    private static Result w1(IntSequence s, int n) {
        Random rnd = new Random(SEED);
        fill(s, n, rnd);
        int[] idx = new int[GET_QUERIES];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = rnd.nextInt(n);
        }
        s.metrics().reset();
        long acc = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < idx.length; i++) {
            acc += s.get(idx[i]);
        }
        long t1 = System.nanoTime();
        sink += acc;
        return result(t1 - t0, s.metrics());
    }

    private static Result w2(IntSequence s, int n) {
        Random rnd = new Random(SEED);
        int[] values = fill(s, n, rnd);
        int[] queries = new int[SEARCH_QUERIES];
        for (int i = 0; i < queries.length; i++) {
            queries[i] = (i % 2 == 0) ? values[rnd.nextInt(n)] : -1 - rnd.nextInt(VALUE_BOUND);
        }
        s.metrics().reset();
        int found = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < queries.length; i++) {
            if (s.contains(queries[i])) {
                found++;
            }
        }
        long t1 = System.nanoTime();
        sink += found;
        if (found < SEARCH_QUERIES / 2) {
            throw new IllegalStateException("present values were not found");
        }
        return result(t1 - t0, s.metrics());
    }

    private static Result w3(IntSequence s, int n, int index) {
        Random rnd = new Random(SEED);
        fill(s, n, rnd);
        int[] extra = new int[UPDATES];
        for (int i = 0; i < extra.length; i++) {
            extra[i] = rnd.nextInt(VALUE_BOUND);
        }
        s.metrics().reset();
        long acc = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < UPDATES; i++) {
            s.add(index, extra[i]);
        }
        for (int i = 0; i < UPDATES; i++) {
            acc += s.remove(index);
        }
        long t1 = System.nanoTime();
        sink += acc;
        return result(t1 - t0, s.metrics());
    }

    private static Result w4(int n) {
        Random rnd = new Random(SEED);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(VALUE_BOUND);
        }
        MinHeap h = new MinHeap();
        int[] out = new int[n];
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            h.insert(values[i]);
        }
        for (int i = 0; i < n; i++) {
            out[i] = h.extractMin();
        }
        long t1 = System.nanoTime();
        for (int i = 1; i < n; i++) {
            if (out[i - 1] > out[i]) {
                throw new IllegalStateException("extractMin order violated at " + i);
            }
        }
        sink += out[n - 1];
        return result(t1 - t0, h.metrics());
    }

    private static Result w5Build(int n) {
        int[] values = randomArray(n);
        long t0 = System.nanoTime();
        MinHeap h = MinHeap.buildHeap(values);
        long t1 = System.nanoTime();
        sink += h.peekMin();
        return result(t1 - t0, h.metrics());
    }

    private static Result w5Insert(int n) {
        int[] values = randomArray(n);
        MinHeap h = new MinHeap(n);
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            h.insert(values[i]);
        }
        long t1 = System.nanoTime();
        sink += h.peekMin();
        return result(t1 - t0, h.metrics());
    }

    private static int[] randomArray(int n) {
        Random rnd = new Random(SEED);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(VALUE_BOUND);
        }
        return values;
    }

    private static Result result(long nanos, Metrics m) {
        return new Result(nanos, m.steps, m.moves, m.comparisons);
    }
}