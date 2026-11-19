import com.example.pawinder_11.logic.feed.AlternativeFeedLogic;
import com.example.pawinder_11.logic.feed.DefaultFeedLogic;
import com.example.pawinder_11.logic.feed.FeedLogic;
import com.example.pawinder_11.logic.feed.FeedLogicContract;
import com.example.pawinder_11.logic.feed.FeedLogicProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class FeedLogicExperiment {
    private static volatile int sink;
    private static final int WARMUPS = 3;
    private static final int REPEATS = 5;
    private static final int ITERATIONS = 500_000;

    private static void verify(FeedLogicContract logic) {
        if (!"/photo.jpg".equals(logic.resolvePhotoUrl("/photo.jpg", ""))) {
            throw new AssertionError("Relative URL test failed");
        }
        if (!"https://example.test/photo.jpg".equals(
                logic.resolvePhotoUrl(
                        " https://example.test/photo.jpg ", ""))) {
            throw new AssertionError("Absolute URL test failed");
        }
        if (logic.resolvePhotoUrl("  ", "") != null) {
            throw new AssertionError("Empty URL test failed");
        }
        if (logic.resolvePhotoUrl(null, "") != null) {
            throw new AssertionError("Null URL test failed");
        }
        if (!logic.isUsableResponse(true, List.of())) {
            throw new AssertionError("Successful empty response rejected");
        }
        if (logic.isUsableResponse(false, List.of())) {
            throw new AssertionError("Failed response accepted");
        }
        if (logic.isUsableResponse(true, null)) {
            throw new AssertionError("Null response accepted");
        }
        if (!FeedLogic.NETWORK_ERROR_PREFIX.equals(
                logic.networkErrorMessage(null))) {
            throw new AssertionError("Null exception test failed");
        }
        if (!(logic.networkErrorMessage(
                new IllegalStateException("test")).endsWith(": test"))) {
            throw new AssertionError("Exception message test failed");
        }
    }

    private static void compare(FeedLogicContract first,
                                FeedLogicContract second) {
        String[] urls = {null, "", "  ", "/photo.jpg",
                "https://example.test/a.jpg", " /b.jpg "};
        for (String url : urls) {
            if (!java.util.Objects.equals(
                    first.resolvePhotoUrl(url, "https://base.test"),
                    second.resolvePhotoUrl(url, "https://base.test"))) {
                throw new AssertionError("URL results differ: " + url);
            }
        }

        List<?>[] bodies = {null, List.of(), List.of("animal")};
        for (boolean successful : new boolean[]{false, true}) {
            for (List<?> body : bodies) {
                if (first.isUsableResponse(successful, body)
                        != second.isUsableResponse(successful, body)) {
                    throw new AssertionError("Response results differ");
                }
            }
        }

        Throwable[] errors = {null, new IllegalStateException(),
                new IllegalStateException("test")};
        for (Throwable error : errors) {
            if (!java.util.Objects.equals(
                    first.networkErrorMessage(error),
                    second.networkErrorMessage(error))) {
                throw new AssertionError("Error messages differ");
            }
        }
    }

    private static long measure(FeedLogicContract logic) {
        long start = System.nanoTime();
        int local = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            String url = logic.resolvePhotoUrl(
                    (i & 1) == 0 ? "/photo.jpg" : "https://example.test/a.jpg",
                    "https://base.test");
            if (url != null) {
                local += url.length();
            }
            if (logic.isUsableResponse(true, List.of())) {
                local++;
            }
            local += logic.networkErrorMessage(null).length();
        }

        sink = local;
        return System.nanoTime() - start;
    }

    private static double median(long[] values) {
        long[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        return sorted[sorted.length / 2] / 1_000_000.0;
    }

    private static double benchmark(String name, FeedLogicContract logic,
                                    StringBuilder csv) {
        for (int i = 0; i < WARMUPS; i++) {
            measure(logic);
        }

        long[] times = new long[REPEATS];
        for (int i = 0; i < REPEATS; i++) {
            times[i] = measure(logic);
        }

        double med = median(times);
        System.out.printf(Locale.US,
                "%s: median=%.3f ms; iterations=%d%n",
                name, med, ITERATIONS);

        for (int i = 0; i < times.length; i++) {
            csv.append(String.format(Locale.US,
                    "%s,%d,%.3f,%.3f%n",
                    name, i + 1, times[i] / 1_000_000.0, med));
        }
        return med;
    }

    public static void main(String[] args) throws Exception {
        FeedLogicContract baseline = new DefaultFeedLogic();
        FeedLogicContract alternative = new AlternativeFeedLogic();
        FeedLogicContract provider = FeedLogicProvider.getInstance();

        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Warm-ups: " + WARMUPS + "; repeats: " + REPEATS);
        System.out.println("Iterations per repeat: " + ITERATIONS);
        System.out.println("Baseline: DefaultFeedLogic");
        System.out.println("Alternative: AlternativeFeedLogic");

        verify(baseline);
        verify(alternative);
        compare(baseline, alternative);

        if (!(provider instanceof DefaultFeedLogic)) {
            throw new AssertionError("Unexpected provider implementation");
        }

        System.out.println("Baseline functional checks: PASS");
        System.out.println("Alternative functional checks: PASS");
        System.out.println("Behavior comparison: PASS");
        System.out.println("Provider check: PASS (DefaultFeedLogic)");

        StringBuilder csv = new StringBuilder(
                "implementation,run,time_ms,median_ms\n");

        double baselineMedian = benchmark("DefaultFeedLogic", baseline, csv);
        double alternativeMedian = benchmark(
                "AlternativeFeedLogic", alternative, csv);

        System.out.printf(Locale.US,
                "Alternative/baseline time ratio: %.3f%n",
                alternativeMedian / baselineMedian);
        System.out.println("Measurement sink: " + sink);

        Path output = Path.of("lab4-experiments", "feed-logic-results.csv");
        Files.writeString(output, csv, StandardCharsets.UTF_8);
        System.out.println("CSV saved: " + output.toAbsolutePath());
    }
}
