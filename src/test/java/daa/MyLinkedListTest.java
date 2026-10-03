package daa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MyLinkedListTest extends AbstractSequenceTest {
    @Override
    IntSequence create() {
        return new MyLinkedList();
    }

    @Test
    void getCostsIndexSteps() {
        MyLinkedList l = new MyLinkedList();
        for (int i = 0; i < 100; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.get(77);
        assertEquals(77, l.metrics().steps);
    }

    @Test
    void headInsertIsConstantTime() {
        MyLinkedList l = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.add(0, -1);
        assertEquals(0, l.metrics().steps);
        assertEquals(2, l.metrics().moves);
    }
}