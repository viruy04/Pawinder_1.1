import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class CpuExperiment {
    private static final int SIZE = 400_000;
    private static final int WARMUPS = 2;
    private static final int REPEATS = 5;

    private static double calculate(int index) {
        double value = index * 0.001 + 1.0;
        for (int i = 0; i < 40; i++) {
            value = Math.sin(value) * Math.cos(value + 0.1) + 1.001;
        }
        return value;
    }

    private static double[] sequential() {
        double[] result = new double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            result[i] = calculate(i);
        }
        return result;
    }

    private static double[] parallel(int threads) {
        double[] result = new double[SIZE];
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.submit(() ->
                IntStream.range(0, SIZE).parallel()
                    .forEach(i -> result[i] = calculate(i))
            ).join();
        } finally {
            pool.shutdown();
        }
        return result;
    }

    private static long measure(boolean parallel, int threads) {
        long start = System.nanoTime();
        if (parallel) {
            parallel(threads);
        } else {
            sequential();
        }
        return System.nanoTime() - start;
    }

    private static double median(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        if (sorted.length % 2 == 1) {
            return sorted[sorted.length / 2] / 1_000_000.0;
        }
        return (sorted[sorted.length / 2 - 1] / 2.0
              + sorted[sorted.length / 2] / 2.0) / 1_000_000.0;
    }

    private static double spread(long[] values, double medianMs) {
        long min = Arrays.stream(values).min().orElseThrow();
        long max = Arrays.stream(values).max().orElseThrow();
        return (max - min) / 1_000_000.0 / medianMs;
    }

    public static void main(String[] args) throws IOException {
        int logicalProcessors = Runtime.getRuntime().availableProcessors();
        int[] threadCounts = {1, 2, 4, logicalProcessors};

        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Logical processors: " + logicalProcessors);
        System.out.println("Array size: " + SIZE);
        System.out.println("Warm-ups: " + WARMUPS + "; repeats: " + REPEATS);

        double[] expected = sequential();

        for (int p : threadCounts) {
            double[] actual = parallel(p);
            if (!Arrays.equals(expected, actual)) {
                throw new IllegalStateException(
                    "Results differ for parallelism=" + p
                );
            }
        }
        System.out.println("Correctness check: PASS");

        long[] sequentialTimes = new long[REPEATS];
        for (int i = 0; i < WARMUPS; i++) {
            measure(false, 1);
        }
        for (int i = 0; i < REPEATS; i++) {
            sequentialTimes[i] = measure(false, 1);
        }

        double sequentialMedian = median(sequentialTimes);
        System.out.printf("Sequential median: %.3f ms%n", sequentialMedian);

        StringBuilder csv = new StringBuilder(
            "mode,parallelism,run,time_ms,median_ms,spread_ratio,"
            + "speedup,efficiency,estimated_sequential_fraction%n"
        );

        for (int p : threadCounts) {
            for (int i = 0; i < WARMUPS; i++) {
                measure(true, p);
            }

            long[] times = new long[REPEATS];
            for (int i = 0; i < REPEATS; i++) {
                times[i] = measure(true, p);
            }

            double med = median(times);
            double variation = spread(times, med);
            double speedup = sequentialMedian / med;
            double efficiency = speedup / p;
            double fraction = p == 1 ? Double.NaN
                : (1.0 / speedup - 1.0 / p) / (1.0 - 1.0 / p);

            System.out.printf(
                "Parallel p=%d: median=%.3f ms; spread=%.3f; "
                + "speedup=%.3f; efficiency=%.3f; Amdahl fraction=%.3f%n",
                p, med, variation, speedup, efficiency, fraction
            );

            for (int i = 0; i < times.length; i++) {
                csv.append(String.format(
                    java.util.Locale.US,
                    "parallel,%d,%d,%.3f,%.3f,%.5f,%.5f,%.5f,%.5f%n",
                    p, i + 1, times[i] / 1_000_000.0, med,
                    variation, speedup, efficiency, fraction
                ));
            }
        }

        for (int i = 0; i < sequentialTimes.length; i++) {
            csv.append(String.format(
                java.util.Locale.US,
                "sequential,1,%d,%.3f,%.3f,%.5f,1.00000,1.00000,NaN%n",
                i + 1, sequentialTimes[i] / 1_000_000.0,
                sequentialMedian, spread(sequentialTimes, sequentialMedian)
            ));
        }

        Path output = Path.of("lab4-experiments", "cpu-results.csv");
        Files.writeString(output, csv, StandardCharsets.UTF_8);
        System.out.println("CSV saved: " + output.toAbsolutePath());
    }
}
