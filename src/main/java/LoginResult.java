
public class LoginResult {

    public enum Status {
        SUCCESS,
        ERROR,
        BLOCKED
    }

    private final Status status;
    private final int remainingAttempts;
    private final long remainingSeconds;

    private LoginResult(
            Status status,
            int remainingAttempts,
            long remainingSeconds) {

        this.status = status;
        this.remainingAttempts = remainingAttempts;
        this.remainingSeconds = remainingSeconds;
    }

    public static LoginResult success() {
        return new LoginResult(Status.SUCCESS, 0, 0);
    }

    public static LoginResult error(int remainingAttempts) {
        return new LoginResult(
                Status.ERROR, remainingAttempts, 0);
    }

    public static LoginResult blocked(long remainingSeconds) {
        return new LoginResult(
                Status.BLOCKED, 0, remainingSeconds);
    }

    public Status getStatus() {
        return status;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
