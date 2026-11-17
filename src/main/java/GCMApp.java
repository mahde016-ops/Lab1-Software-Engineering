
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GCMApp extends Application {

    private int maxAttempts;
    private int blockDuration;

    private UserRepository repository;
    private AttemptTracker tracker;

    private BlockingQueue<FailedAttempt> failureQueue;

    private FailureWorker worker1;
    private FailureWorker worker2;

    private LoginController controller;

    @Override
    public void init() throws Exception {
        // Load users outside the JavaFX UI thread.
        repository = new UserRepository();
    }

    @Override
    public void start(Stage stage) throws Exception {

        List<String> args = getParameters().getRaw();

        maxAttempts = readPositiveInt(
                args, 0, "n (maximum attempts)"
        );

        blockDuration = readPositiveInt(
                args, 1, "t (block duration in seconds)"
        );

        System.out.println("Max attempts: " + maxAttempts);
        System.out.println("Block duration: " + blockDuration);

        tracker = new AttemptTracker(
                maxAttempts,
                blockDuration
        );

        failureQueue = new LinkedBlockingQueue<>();

        // Create exactly two daemon failure workers.
        worker1 = new FailureWorker(
                failureQueue,
                tracker,
                "FailureWorker-1"
        );

        worker2 = new FailureWorker(
                failureQueue,
                tracker,
                "FailureWorker-2"
        );

        worker1.start();
        worker2.start();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml")
        );

        Scene scene = new Scene(loader.load());

        controller = loader.getController();

        controller.configure(
                repository,
                tracker,
                failureQueue
        );

        stage.setTitle("GCM - Login");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            controller.shutdown();
            worker1.interrupt();
            worker2.interrupt();
        });

        stage.show();

        System.out.println("Failure workers started.");
    }

    private int readPositiveInt(
            List<String> args,
            int index,
            String name) {

        if (index < args.size()) {
            try {
                int value = Integer.parseInt(args.get(index));

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException ignored) {
                // Ask for a valid value.
            }
        }

        while (true) {

            TextInputDialog dialog = new TextInputDialog();

            dialog.setTitle("Invalid Arguments");
            dialog.setHeaderText(
                    "Missing or invalid: " + name
            );

            dialog.setContentText(
                    "Enter a positive integer:"
            );

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                javafx.application.Platform.exit();
                throw new IllegalStateException(
                        "Application startup cancelled"
                );
            }

            try {
                int value = Integer.parseInt(
                        result.get().trim()
                );

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException ignored) {
                // Ask again.
            }
        }
    }

    @Override
    public void stop() {

        if (controller != null) {
            controller.shutdown();
        }

        if (worker1 != null) {
            worker1.interrupt();
        }

        if (worker2 != null) {
            worker2.interrupt();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
