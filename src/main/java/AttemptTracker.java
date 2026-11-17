import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

public class AttemptTracker {

    private final ConcurrentHashMap<String, UserState> states;
    private final int maxAttempts;
    private final int blockSeconds;
    private final ConcurrentHashMap<String, ReentrantLock> loginLocks =
            new ConcurrentHashMap<>();
    public AttemptTracker(int maxAttempts, int blockSeconds) {
        if (maxAttempts < 1 || blockSeconds < 1) {
            throw new IllegalArgumentException(
                    "Arguments must be positive integers"
            );
        }

        this.maxAttempts = maxAttempts;
        this.blockSeconds = blockSeconds;
        this.states = new ConcurrentHashMap<>();
    }

    private UserState getState(String email) {
        return states.computeIfAbsent(
                email,
                key -> new UserState()
        );
    }

    public boolean isBlocked(String email) {
        return getState(email).isBlocked();
    }

    public int recordFailure(String email) {
        return getState(email).recordFailure(
                maxAttempts,
                blockSeconds
        );
    }

    public long getRemainingBlockSeconds(String email) {
        return getState(email).getRemainingBlockSeconds();
    }

    public void reset(String email) {
        getState(email).reset();
    }

    public int getFailedAttempts(String email) {
        return getState(email).getFailedAttempts();
    }

    public ReentrantLock getLoginLock(String email) {
        return loginLocks.computeIfAbsent(
                email,
                key -> new ReentrantLock()
        );
    }

}
