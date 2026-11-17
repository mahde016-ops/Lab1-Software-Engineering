
public class BlockCheckThread extends Thread {

    private final AttemptTracker tracker;
    private final String email;

    private boolean blocked;
    private long remainingSeconds;

    public BlockCheckThread(AttemptTracker tracker, String email) {
        this.tracker = tracker;
        this.email = email;
    }

    @Override
    public void run() {
        blocked = tracker.isBlocked(email);

        if (blocked) {
            remainingSeconds =
                    tracker.getRemainingBlockSeconds(email);
        }
    }

    public boolean isAccountBlocked() {
        return blocked;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
