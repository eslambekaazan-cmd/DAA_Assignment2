# DAA Assignment 2 - In-Memory Workload Engine

Own implementations (no `java.util` collections, `int` storage only) of:
`DynamicArray`, `MyLinkedList` (singly linked, head + tail) and `MinHeap` (array-based binary heap),
plus operation counters (`steps`, `moves`, `comparisons`), a benchmark and JUnit 5 tests.

Requirements: JDK 17+, Maven 3.8+, Python 3 with matplotlib (only for the plots).

## Build and test
```bash
mvn clean test          # compiles and runs all JUnit 5 tests
mvn clean package       # builds target/classes and the jar
```

## Run the benchmark (one command, seed 42, writes results/results.csv)
```bash
mvn -q compile && java -cp target/classes daa.Benchmark
# or: mvn -q compile exec:java
```
Each case: 3 warm-up runs (discarded) + 5 measured runs, the median time is saved.
Counters are collected inside the data-structure methods; the fill phase is not counted or timed.
Bonus rows (workload `W5`) compare Floyd's `buildHeap` with n separate `insert` calls.

## Plots
```bash
python3 scripts/plot.py      # reads results/results.csv, writes results/plots/*.png
```

## Layout
```
src/main/java/daa   Metrics, IntSequence, DynamicArray, MyLinkedList, MinHeap, Benchmark
src/test/java/daa   AbstractSequenceTest (shared), DynamicArrayTest, MyLinkedListTest, MinHeapTest
results/            results.csv, plots/
scripts/plot.py     chart generation
REPORT.md           complexity table, loop invariant proofs, plots, discussion
```

Counter definitions: *steps* = one array-cell read or one move to the next node; *moves* = one element
shifted/copied in an array or one pointer update in the list; *comparisons* = one comparison of two elements.
