import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private UserRepository repository;
    private AttemptTracker tracker;
    private BlockingQueue<FailedAttempt> failureQueue;

    private Thread loginThread;
    private Thread countdownThread;

    public void configure(
            UserRepository repository,
            AttemptTracker tracker,
            BlockingQueue<FailedAttempt> failureQueue) {

        this.repository = repository;
        this.tracker = tracker;
        this.failureQueue = failureQueue;
    }

    @FXML
    private void handleLogin() {

        if (loginButton.isDisabled()) {
            return;
        }

        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setText("");
        setInputsDisabled(true);

        LoginTask task = new LoginTask(
                email,
                password,
                repository,
                tracker,
                failureQueue,
                this::handleLoginResult
        );

        loginThread = new Thread(task, "LoginTask");
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void handleLoginResult(LoginResult result) {

        switch (result.getStatus()) {

            case SUCCESS:
                stopCountdown();
                openWelcomeScreen();
                break;

            case ERROR:
                if (result.getRemainingAttempts() < 0) {
                    errorLabel.setText("Invalid credentials");
                } else {
                    errorLabel.setText(
                            "Invalid credentials. Attempts left: "
                                    + result.getRemainingAttempts()
                    );
                }

                setInputsDisabled(false);
                break;

            case BLOCKED:
                startCountdown();
                break;
        }
    }

    private void startCountdown() {

        stopCountdown();

        String email = usernameField.getText().trim();

        CountdownTask task = new CountdownTask(
                email,
                tracker,

                remaining -> {
                    errorLabel.setText(
                            "Account blocked. " +
                                    remaining + " seconds remaining"
                    );
                },

                () -> {
                    errorLabel.setText("");
                    setInputsDisabled(false);
                    countdownThread = null;
                }
        );

        countdownThread = new Thread(
                task,
                "CountdownTask"
        );

        countdownThread.setDaemon(true);
        countdownThread.start();
    }

    private void stopCountdown() {

        if (countdownThread != null) {
            countdownThread.interrupt();
            countdownThread = null;
        }
    }

    private void setInputsDisabled(boolean disabled) {

        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
    }

    private void openWelcomeScreen() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/welcome.fxml")
            );

            Scene welcomeScene = new Scene(loader.load());

            Stage stage = (Stage)
                    usernameField.getScene().getWindow();

            stage.setTitle("GCM - Welcome");
            stage.setScene(welcomeScene);

        } catch (IOException e) {
            errorLabel.setText("Could not open Welcome screen");
            setInputsDisabled(false);
        }
    }

    @FXML

    private void handleConcurrencyTest() {

        errorLabel.setText("Running concurrency test...");

        Thread testThread = new Thread(() -> {

            try {
                String report = ConcurrencyTest.runTest();

                boolean passed = report.contains("Overall: PASS");

                Platform.runLater(() -> {
                    errorLabel.setText(
                            passed
                                    ? "Concurrency test PASSED! Check console."
                                    : "Concurrency test FAILED! Check console."
                    );
                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() ->
                        errorLabel.setText(
                                "Concurrency test error. Check console."
                        )
                );
            }

        }, "ConcurrencyTestRunner");

        testThread.setDaemon(true);
        testThread.start();
    }


    public void shutdown() {

        stopCountdown();

        if (loginThread != null) {
            loginThread.interrupt();
        }
    }
}
