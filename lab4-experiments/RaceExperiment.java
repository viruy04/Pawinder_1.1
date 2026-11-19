import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceExperiment {
    private static final int THREADS = 8;
    private static final int INCREMENTS = 100_000;
    private static final int ROUNDS = 10;

    private static int unsafeCounter;
    private static int safeCounter;

    private static void unsafeIncrement() {
        unsafeCounter++;
    }

    private static synchronized void safeIncrement() {
        safeCounter++;
    }Ы

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

    private static long runSafe() throws Exception {
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
            return System.nanoTime() - begin;
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        int expected = THREADS * INCREMENTS;
        int violations = 0;
        long unsafeTotal = 0;
        long safeTotal = 0;

        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Threads: " + THREADS);
        System.out.println("Increments per thread: " + INCREMENTS);
        System.out.println("Expected invariant: counter = " + expected);
        System.out.println("Rounds: " + ROUNDS);

        for (int round = 1; round <= ROUNDS; round++) {
            long unsafeTime = runUnsafe();
            boolean violation = unsafeCounter != expected;
            if (violation) {
                violations++;
            }
            unsafeTotal += unsafeTime;

            System.out.printf(
                "Race round %d: counter=%d; invariant=%s; time=%.3f ms%n",
                round, unsafeCounter, violation ? "VIOLATED" : "OK",
                unsafeTime / 1_000_000.0
            );

            long safeTime = runSafe();
            safeTotal += safeTime;
            if (safeCounter != expected) {
                throw new IllegalStateException(
                    "Invariant violated after synchronization!"
                );
            }
        }

        System.out.printf(
            "Race frequency: %d/%d (%.1f%%)%n",
            violations, ROUNDS, violations * 100.0 / ROUNDS
        );
        System.out.printf(
            "Unsafe mean time: %.3f ms%n",
            unsafeTotal / ROUNDS / 1_000_000.0
        );
        System.out.printf(
            "Synchronized mean time: %.3f ms%n",
            safeTotal / ROUNDS / 1_000_000.0
        );
        System.out.println(
            "Fixed invariant: PASS in all " + ROUNDS + " rounds"
        );
    }
}
