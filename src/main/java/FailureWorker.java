
import java.util.concurrent.BlockingQueue;

public class FailureWorker extends Thread {

    private final BlockingQueue<FailedAttempt> queue;
    private final AttemptTracker tracker;

    public FailureWorker(
            BlockingQueue<FailedAttempt> queue,
            AttemptTracker tracker,
            String name) {

        super(name);

        this.queue = queue;
        this.tracker = tracker;

        setDaemon(true);
    }

    @Override
    public void run() {

        try {
            while (!isInterrupted()) {

                FailedAttempt attempt = queue.take();

                int remaining = tracker.recordFailure(
                        attempt.getEmail()
                );

                attempt.sendResponse(remaining);
            }

        } catch (InterruptedException e) {
            interrupt();
        }
    }
}
