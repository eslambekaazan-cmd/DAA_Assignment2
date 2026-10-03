package daa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DynamicArrayTest extends AbstractSequenceTest {
    @Override
    IntSequence create() {
        return new DynamicArray();
    }

    @Test
    void getCostsOneStep() {
        DynamicArray a = new DynamicArray();
        for (int i = 0; i < 100; i++) {
            a.add(i);
        }
        a.metrics().reset();
        a.get(77);
        assertEquals(1, a.metrics().steps);
    }

    @Test
    void headInsertShiftsEverything() {
        DynamicArray a = new DynamicArray(1000);
        for (int i = 0; i < 100; i++) {
            a.add(i);
        }
        a.metrics().reset();
        a.add(0, -1);
        assertEquals(100, a.metrics().moves);
    }
}