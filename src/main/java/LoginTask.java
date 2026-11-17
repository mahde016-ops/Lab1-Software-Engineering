
import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class LoginTask implements Runnable {

    private final String email;
    private final String password;
    private final UserRepository repository;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failureQueue;
    private final Consumer<LoginResult> onResult;

    public LoginTask(
            String email,
            String password,
            UserRepository repository,
            AttemptTracker tracker,
            BlockingQueue<FailedAttempt> failureQueue,
            Consumer<LoginResult> onResult) {

        this.email = email;
        this.password = password;
        this.repository = repository;
        this.tracker = tracker;
        this.failureQueue = failureQueue;
        this.onResult = onResult;
    }

    @Override
    public void run() {

        LoginResult result;

        ReentrantLock lock = tracker.getLoginLock(email);

        try {
            lock.lockInterruptibly();

            try {
                // Check lockout status in a separate thread.
                BlockCheckThread checker =
                        new BlockCheckThread(tracker, email);

                checker.start();
                checker.join();

                if (checker.isAccountBlocked()) {

                    result = LoginResult.blocked(
                            checker.getRemainingSeconds()
                    );

                } else if (repository.authenticate(email, password)) {

                    tracker.reset(email);
                    result = LoginResult.success();

                } else if (!repository.emailExists(email)) {

                    // Unknown accounts are not counted.
                    result = LoginResult.error(-1);

                } else {

                    // Send the failure to the worker queue.
                    FailedAttempt attempt = new FailedAttempt(email);

                    failureQueue.put(attempt);

                    int remaining = attempt.waitForResponse();

                    if (remaining == 0) {
                        result = LoginResult.blocked(
                                tracker.getRemainingBlockSeconds(email)
                        );
                    } else {
                        result = LoginResult.error(remaining);
                    }
                }

            } finally {
                lock.unlock();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;

        } catch (IllegalArgumentException e) {
            result = LoginResult.error(-1);
        }

        LoginResult finalResult = result;

        Platform.runLater(() -> onResult.accept(finalResult));
    }
}
