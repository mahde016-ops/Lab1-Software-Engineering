
import javafx.application.Platform;

import java.util.function.Consumer;

public class CountdownTask implements Runnable {

    private final String email;
    private final AttemptTracker tracker;
    private final Consumer<Long> onTick;
    private final Runnable onFinished;

    public CountdownTask(
            String email,
            AttemptTracker tracker,
            Consumer<Long> onTick,
            Runnable onFinished) {

        this.email = email;
        this.tracker = tracker;
        this.onTick = onTick;
        this.onFinished = onFinished;
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {

                long remaining =
                        tracker.getRemainingBlockSeconds(email);

                if (remaining <= 0) {
                    Platform.runLater(onFinished);
                    return;
                }

                Platform.runLater(() -> onTick.accept(remaining));

                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
