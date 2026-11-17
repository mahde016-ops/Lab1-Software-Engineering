
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrencyTest {

    private static final int THREAD_COUNT = 20;
    private static final int FAILURES_PER_THREAD = 499;
    private static final int MAX_ATTEMPTS = 10000;
    private static final int BLOCK_SECONDS = 30;

    public static String runTest() throws InterruptedException {

        // Use a separate tracker so the normal login is unaffected.
        AttemptTracker tracker =
                new AttemptTracker(MAX_ATTEMPTS, BLOCK_SECONDS);

        String email = "concurrency@test.com";

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        List<Thread> threads = new ArrayList<>();

        long startTime = System.nanoTime();

        // Create 20 threads.
        for (int i = 0; i < THREAD_COUNT; i++) {

            Thread thread = new Thread(() -> {
                try {
                    startSignal.await();

                    for (int j = 0; j < FAILURES_PER_THREAD; j++) {
                        tracker.recordFailure(email);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    error.compareAndSet(null, e);

                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                }
            }, "TestThread-" + (i + 1));

            threads.add(thread);
            thread.start();
        }

        // Release all threads together.
        startSignal.countDown();

        // Wait until all 20 threads finish.
        for (Thread thread : threads) {
            thread.join();
        }

        if (error.get() != null) {
            throw new IllegalStateException(
                    "A test thread failed", error.get());
        }

        int expectedFailures = THREAD_COUNT * FAILURES_PER_THREAD;
        int actualFailures = tracker.getFailedAttempts(email);

        boolean firstStagePassed =
                actualFailures == expectedFailures
                        && !tracker.isBlocked(email);

        // Add another 19 failures.
        for (int i = 0; i < 19; i++) {
            tracker.recordFailure(email);
        }

        int beforeFinalFailure = tracker.getFailedAttempts(email);

        boolean secondStagePassed =
                beforeFinalFailure == 9999
                        && !tracker.isBlocked(email);

        // Failure number 10,000 must trigger the lockout.
        tracker.recordFailure(email);

        boolean thirdStagePassed =
                tracker.isBlocked(email);

        long durationMs =
                (System.nanoTime() - startTime) / 1_000_000;

        String report =
                "\n========== CONCURRENCY TEST ==========\n"
                        + "Threads: " + THREAD_COUNT + "\n"
                        + "Failures per thread: " + FAILURES_PER_THREAD + "\n"
                        + "Expected failures: " + expectedFailures + "\n"
                        + "Actual failures: " + actualFailures + "\n"
                        + "Stage 1 (9980): "
                        + (firstStagePassed ? "PASS" : "FAIL") + "\n"
                        + "After 19 additional failures: "
                        + beforeFinalFailure + "\n"
                        + "Stage 2 (9999, not blocked): "
                        + (secondStagePassed ? "PASS" : "FAIL") + "\n"
                        + "Stage 3 (10000, blocked): "
                        + (thirdStagePassed ? "PASS" : "FAIL") + "\n"
                        + "Duration: " + durationMs + " ms\n"
                        + "Overall: "
                        + (firstStagePassed && secondStagePassed && thirdStagePassed
                        ? "PASS" : "FAIL")
                        + "\n======================================\n";

        System.out.println(report);

        return report;
    }
}
