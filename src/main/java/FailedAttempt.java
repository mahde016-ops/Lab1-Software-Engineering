
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FailedAttempt {

    private final String email;
    private final BlockingQueue<Integer> responseQueue =
            new ArrayBlockingQueue<>(1);

    public FailedAttempt(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void sendResponse(int remainingAttempts)
            throws InterruptedException {
        responseQueue.put(remainingAttempts);
    }

    public int waitForResponse()
            throws InterruptedException {
        return responseQueue.take();
    }
}
