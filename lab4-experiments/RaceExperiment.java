import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Arrays;

public class RaceExperiment {
    private static final int THREADS = 8;
    private static final int INCREMENTS = 100_000;
    private static final int ROUNDS = 10;
    private static final int WARMUPS = 2;
    private static final int REPEATS = 5;

    private static int unsafeCounter;
    private static int safeCounter;

    private static void unsafeIncrement() {
        unsafeCounter++;
    }

    private static synchronized void safeIncrement() {
        safeCounter++;
    }

    private static long runUnsafe() throws Exception {
        unsafeCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        Future<?>[] tasks = new Future<?>[THREADS];

        long begin = System.nanoTime();
        try {
            for (int t = 0; t < THREADS; t++) {
                tasks[t] = pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int i = 0; i < INCREMENTS; i++) {
                            unsafeIncrement();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                });
            }

            ready.await();
            start.countDown();
            for (Future<?> task : tasks) {
                task.get();
            }
            return System.nanoTime() - begin;
        } finally {
            pool.shutdown();
        }
    }

    private static long runSynchronized() throws Exception {
        safeCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        Future<?>[] tasks = new Future<?>[THREADS];

        long begin = System.nanoTime();
        try {
            for (int t = 0; t < THREADS; t++) {
                tasks[t] = pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int i = 0; i < INCREMENTS; i++) {
                            safeIncrement();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                });
            }

            ready.await();
            start.countDown();
            for (Future<?> task : tasks) {
                task.get();
            }

            if (safeCounter != THREADS * INCREMENTS) {
                throw new IllegalStateException("Synchronized result is incorrect");
            }
            return System.nanoTime() - begin;
        } finally {
            pool.shutdown();
        }
    }

    private static long runLocalCounters() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        Future<Integer>[] tasks = new Future[THREADS];

        long begin = System.nanoTime();
        try {
            for (int t = 0; t < THREADS; t++) {
                tasks[t] = pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    int localCounter = 0;
                    for (int i = 0; i < INCREMENTS; i++) {
                        localCounter++;
                    }
                    return localCounter;
                });
            }

            ready.await();
            start.countDown();

            int total = 0;
            for (Future<Integer> task : tasks) {
                total += task.get();
            }

            if (total != THREADS * INCREMENTS) {
                throw new IllegalStateException("Local-counter result is incorrect");
            }
            return System.nanoTime() - begin;
        } finally {
            pool.shutdown();
        }
    }

    private static double medianMs(long[] times) {
        Arrays.sort(times);
        return times[times.length / 2] / 1_000_000.0;
    }

    public static void main(String[] args) throws Exception {
        int expected = THREADS * INCREMENTS;
        int violations = 0;

        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Threads: " + THREADS);
        System.out.println("Increments per thread: " + INCREMENTS);
        System.out.println("Expected invariant: counter = " + expected);
        System.out.println("Race reproduction rounds: " + ROUNDS);

        for (int round = 1; round <= ROUNDS; round++) {
            runUnsafe();
            boolean violation = unsafeCounter != expected;

            if (violation) {
                violations++;
            }

            System.out.printf(
                "Race round %d: counter=%d; invariant=%s%n",
                round, unsafeCounter, violation ? "VIOLATED" : "OK"
            );
        }

        System.out.printf(
            "Race frequency: %d/%d (%.1f%%)%n",
            violations, ROUNDS, violations * 100.0 / ROUNDS
        );

        for (int i = 0; i < WARMUPS; i++) {
            runSynchronized();
            runLocalCounters();
        }

        long[] synchronizedTimes = new long[REPEATS];
        long[] localTimes = new long[REPEATS];

        for (int i = 0; i < REPEATS; i++) {
            synchronizedTimes[i] = runSynchronized();
            localTimes[i] = runLocalCounters();
        }

        double synchronizedMedian = medianMs(synchronizedTimes);
        double localMedian = medianMs(localTimes);

        System.out.println("Correctness check: PASS for both variants");
        System.out.println("Warm-ups: " + WARMUPS + "; repeats: " + REPEATS);
        System.out.printf(
            "Synchronized median: %.3f ms%n", synchronizedMedian
        );
        System.out.printf(
            "Local counters median: %.3f ms%n", localMedian
        );
        System.out.printf(
            "Synchronized/local time ratio: %.3f%n",
            synchronizedMedian / localMedian
        );
    }
}
