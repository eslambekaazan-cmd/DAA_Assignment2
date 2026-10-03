package daa;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

abstract class AbstractSequenceTest {
    abstract IntSequence create();

    @Test
    void emptyStructure() {
        IntSequence s = create();
        assertEquals(0, s.size());
        assertFalse(s.contains(1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(1, 5));
        s.add(0, 5);
        assertEquals(1, s.size());
        assertEquals(5, s.get(0));
    }

    @Test
    void oneElement() {
        IntSequence s = create();
        s.add(42);
        assertEquals(42, s.get(0));
        assertTrue(s.contains(42));
        assertEquals(42, s.remove(0));
        assertEquals(0, s.size());
        assertFalse(s.contains(42));
        s.add(7);
        assertEquals(7, s.get(0));
    }

    @Test
    void duplicates() {
        IntSequence s = create();
        s.add(7);
        s.add(7);
        s.add(7);
        assertEquals(7, s.remove(1));
        assertEquals(2, s.size());
        assertTrue(s.contains(7));
        s.remove(0);
        s.remove(0);
        assertFalse(s.contains(7));
    }

    @Test
    void firstAndLastIndex() {
        IntSequence s = create();
        for (int i = 0; i < 5; i++) {
            s.add(i);
        }
        s.add(0, 100);
        s.add(s.size(), 200);
        assertEquals(100, s.get(0));
        assertEquals(200, s.get(s.size() - 1));
        assertEquals(200, s.remove(s.size() - 1));
        assertEquals(100, s.remove(0));
        assertEquals(0, s.get(0));
        assertEquals(4, s.get(s.size() - 1));
        s.add(77);
        assertEquals(77, s.get(s.size() - 1));
    }

    @Test
    void invalidIndex() {
        IntSequence s = create();
        s.add(1);
        s.add(2);
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(2));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(3, 0));
        assertEquals(2, s.size());
    }

    @Test
    void growsPastInitialCapacity() {
        IntSequence s = create();
        for (int i = 0; i < 1000; i++) {
            s.add(i);
        }
        assertEquals(1000, s.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, s.get(i));
        }
    }

    @Test
    void randomOperationsMatchArrayList() {
        Random rnd = new Random(1);
        IntSequence s = create();
        List<Integer> expected = new ArrayList<>();
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(5);
            if (op == 0) {
                int x = rnd.nextInt(50);
                s.add(x);
                expected.add(x);
            } else if (op == 1) {
                int idx = rnd.nextInt(expected.size() + 1);
                int x = rnd.nextInt(50);
                s.add(idx, x);
                expected.add(idx, x);
            } else if (op == 2 && !expected.isEmpty()) {
                int idx = rnd.nextInt(expected.size());
                assertEquals(expected.remove(idx).intValue(), s.remove(idx));
            } else if (op == 3 && !expected.isEmpty()) {
                int idx = rnd.nextInt(expected.size());
                assertEquals(expected.get(idx).intValue(), s.get(idx));
            } else {
                int x = rnd.nextInt(60);
                assertEquals(expected.contains(x), s.contains(x));
            }
            assertEquals(expected.size(), s.size());
        }
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).intValue(), s.get(i));
        }
    }

    @Test
    void countersAreCountedInsideOperations() {
        IntSequence s = create();
        for (int i = 0; i < 100; i++) {
            s.add(i);
        }
        s.metrics().reset();
        s.add(0, -1);
        assertTrue(s.metrics().moves > 0, "insertion at the head must report moves");
        s.metrics().reset();
        s.remove(0);
        assertTrue(s.metrics().moves > 0, "removal at the head must report moves");
        s.metrics().reset();
        s.contains(-5);
        assertEquals(100, s.metrics().comparisons);
    }
}