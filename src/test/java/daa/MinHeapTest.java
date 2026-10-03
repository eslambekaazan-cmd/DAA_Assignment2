package daa;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {
    private static void assertHeapProperty(MinHeap h) {
        int[] a = h.toArray();
        for (int child = 1; child < a.length; child++) {
            int parent = (child - 1) / 2;
            assertTrue(a[parent] <= a[child], "heap property broken at index " + child);
        }
    }

    @Test
    void emptyHeapThrows() {
        MinHeap h = new MinHeap();
        assertTrue(h.isEmpty());
        assertThrows(IllegalStateException.class, h::peekMin);
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    void oneElement() {
        MinHeap h = new MinHeap();
        h.insert(5);
        assertEquals(5, h.peekMin());
        assertEquals(5, h.extractMin());
        assertTrue(h.isEmpty());
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    void duplicates() {
        MinHeap h = new MinHeap();
        for (int i = 0; i < 10; i++) {
            h.insert(3);
        }
        h.insert(1);
        assertEquals(1, h.extractMin());
        for (int i = 0; i < 10; i++) {
            assertEquals(3, h.extractMin());
        }
    }

    @Test
    void heapPropertyAfterEveryInsertAndExtract() {
        Random rnd = new Random(7);
        MinHeap h = new MinHeap(2);
        for (int i = 0; i < 2000; i++) {
            h.insert(rnd.nextInt(500));
            assertHeapProperty(h);
        }
        while (!h.isEmpty()) {
            h.extractMin();
            assertHeapProperty(h);
        }
    }

    @Test
    void peekMinIsSmallestAndDoesNotRemove() {
        MinHeap h = new MinHeap();
        h.insert(9);
        h.insert(4);
        h.insert(6);
        assertEquals(4, h.peekMin());
        assertEquals(3, h.size());
    }

    @Test
    void sortedOutputMatchesArraysSort() {
        Random rnd = new Random(42);
        int n = 10_000;
        int[] values = new int[n];
        MinHeap h = new MinHeap();
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(1000) - 500;
            h.insert(values[i]);
        }
        int[] expected = values.clone();
        Arrays.sort(expected);
        for (int i = 0; i < n; i++) {
            assertEquals(expected[i], h.extractMin());
        }
        assertTrue(h.isEmpty());
    }

    @Test
    void buildHeapMatchesInserts() {
        Random rnd = new Random(3);
        for (int n : new int[] {0, 1, 2, 3, 10, 1000}) {
            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = rnd.nextInt(100);
            }
            MinHeap h = MinHeap.buildHeap(values);
            assertHeapProperty(h);
            int[] expected = values.clone();
            Arrays.sort(expected);
            for (int i = 0; i < n; i++) {
                assertEquals(expected[i], h.extractMin());
            }
        }
    }

    @Test
    void buildHeapUsesFewerComparisonsThanInserts() {
        int n = 10_000;
        Random rnd = new Random(5);
        int[] values = new int[n];
        MinHeap inserted = new MinHeap(n);
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(1_000_000);
            inserted.insert(values[i]);
        }
        MinHeap built = MinHeap.buildHeap(values);
        assertTrue(built.metrics().comparisons < 2L * n, "buildHeap must be O(n): <= 2n comparisons");
        assertTrue(built.metrics().comparisons < inserted.metrics().comparisons);
    }
}