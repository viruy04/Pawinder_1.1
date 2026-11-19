import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IoExperiment {
    private static final int FILE_SIZE = 32 * 1024 * 1024;
    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int WARMUPS = 2;
    private static final int REPEATS = 5;
    private static final Path FILE = Path.of("lab4-experiments", "io-test.bin");

    private static void prepareFile() throws IOException {
        byte[] data = new byte[BUFFER_SIZE];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 31 + 7);
        }

        try (BufferedOutputStream out = new BufferedOutputStream(
                Files.newOutputStream(FILE))) {
            for (int written = 0; written < FILE_SIZE; written += data.length) {
                out.write(data);
            }
        }
    }

    private static long readSequential() throws IOException {
        long total = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        try (BufferedInputStream in = new BufferedInputStream(
                Files.newInputStream(FILE))) {
            int count;
            while ((count = in.read(buffer)) != -1) {
                total += count;
            }
        }
        return total;
    }

    private static long readPart(long start, long length) throws IOException {
        long total = 0;
        ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);

        try (FileChannel channel = FileChannel.open(FILE, StandardOpenOption.READ)) {
            long position = start;
            long end = start + length;

            while (position < end) {
                buffer.clear();
                buffer.limit((int) Math.min(buffer.capacity(), end - position));
                int count = channel.read(buffer, position);

                if (count < 0) {
                    break;
                }
                if (count == 0) {
                    continue;
                }

                total += count;
                position += count;
            }
        }
        return total;
    }

    private static long readConcurrent(int concurrency) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        List<Future<Long>> futures = new ArrayList<>();

        try {
            long partSize = FILE_SIZE / concurrency;
            for (int i = 0; i < concurrency; i++) {
                final long start = i * partSize;
                final long length = (i == concurrency - 1)
                        ? FILE_SIZE - start : partSize;
                futures.add(pool.submit(() -> readPart(start, length)));
            }

            long total = 0;
            for (Future<Long> future : futures) {
                total += future.get();
            }
            return total;
        } finally {
            pool.shutdown();
        }
    }

    private static long measure(boolean concurrent, int concurrency)
            throws Exception {
        long start = System.nanoTime();
        long bytes = concurrent
                ? readConcurrent(concurrency)
                : readSequential();

        if (bytes != FILE_SIZE) {
            throw new IllegalStateException(
                    "Expected " + FILE_SIZE + " bytes, read " + bytes);
        }
        return System.nanoTime() - start;
    }

    private static double median(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        int middle = sorted.length / 2;
        if (sorted.length % 2 == 1) {
            return sorted[middle] / 1_000_000.0;
        }
        return (sorted[middle - 1] / 2.0 + sorted[middle] / 2.0)
                / 1_000_000.0;
    }

    private static double throughput(double timeMs) {
        return (FILE_SIZE / (1024.0 * 1024.0)) / (timeMs / 1000.0);
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(FILE.getParent());
        prepareFile();

        int processors = Runtime.getRuntime().availableProcessors();
        int[] concurrencyValues = {1, 2, 4, processors};

        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Logical processors: " + processors);
        System.out.printf(Locale.US, "File size: %.1f MiB%n",
                FILE_SIZE / (1024.0 * 1024.0));
        System.out.println("Warm-ups: " + WARMUPS + "; repeats: " + REPEATS);
        System.out.println("Cache state: warm; repeated reads after warm-up");
        System.out.println("I/O verification: "
                + (readSequential() == FILE_SIZE ? "PASS" : "FAIL"));

        StringBuilder csv = new StringBuilder(
                "mode,concurrency,run,time_ms,median_ms,throughput_mib_s,"
                        + "cache_state,file_size_mib\n");

        long[] syncTimes = new long[REPEATS];
        for (int i = 0; i < WARMUPS; i++) {
            measure(false, 1);
        }
        for (int i = 0; i < REPEATS; i++) {
            syncTimes[i] = measure(false, 1);
        }

        double syncMedian = median(syncTimes);
        double syncThroughput = throughput(syncMedian);
        System.out.printf(Locale.US,
                "Synchronous: median=%.3f ms; throughput=%.2f MiB/s%n",
                syncMedian, syncThroughput);

        for (int i = 0; i < syncTimes.length; i++) {
            csv.append(String.format(Locale.US,
                    "synchronous,1,%d,%.3f,%.3f,%.3f,warm,%.1f%n",
                    i + 1, syncTimes[i] / 1_000_000.0,
                    syncMedian, syncThroughput,
                    FILE_SIZE / (1024.0 * 1024.0)));
        }

        for (int concurrency : concurrencyValues) {
            for (int i = 0; i < WARMUPS; i++) {
                measure(true, concurrency);
            }

            long[] times = new long[REPEATS];
            for (int i = 0; i < REPEATS; i++) {
                times[i] = measure(true, concurrency);
            }

            double med = median(times);
            double rate = throughput(med);
            System.out.printf(Locale.US,
                    "Concurrent c=%d: median=%.3f ms; throughput=%.2f MiB/s; "
                            + "speedup=%.3f%n",
                    concurrency, med, rate, syncMedian / med);

            for (int i = 0; i < times.length; i++) {
                csv.append(String.format(Locale.US,
                        "concurrent,%d,%d,%.3f,%.3f,%.3f,warm,%.1f%n",
                        concurrency, i + 1, times[i] / 1_000_000.0,
                        med, rate, FILE_SIZE / (1024.0 * 1024.0)));
            }
        }

        Path output = Path.of("lab4-experiments", "io-results.csv");
        Files.writeString(output, csv);
        System.out.println("CSV saved: " + output.toAbsolutePath());
    }
}
