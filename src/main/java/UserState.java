
public class UserState {

    private int failedAttempts = 0;
    private long blockedUntil = 0;

    public synchronized boolean isBlocked() {
        refreshBlockStatus();
        return blockedUntil > System.currentTimeMillis();
    }

    public synchronized int recordFailure(int maxAttempts, int blockSeconds) {        refreshBlockStatus();

        if (blockedUntil > System.currentTimeMillis()) {
            return 0;
        }

        failedAttempts++;

        if (failedAttempts >= maxAttempts) {
            blockedUntil = System.currentTimeMillis()
                    + blockSeconds * 1000L;
            return 0;
        }

        return maxAttempts - failedAttempts;
    }

    public synchronized long getRemainingBlockSeconds() {
        refreshBlockStatus();

        if (blockedUntil == 0) {
            return 0;
        }

        long remaining = blockedUntil - System.currentTimeMillis();

        return Math.max(0, (remaining + 999) / 1000);
    }

    public synchronized void reset() {
        failedAttempts = 0;
        blockedUntil = 0;
    }

    public synchronized int getFailedAttempts() {
        refreshBlockStatus();
        return failedAttempts;
    }

    private synchronized void refreshBlockStatus() {
        if (blockedUntil != 0
                && System.currentTimeMillis() >= blockedUntil) {
            blockedUntil = 0;
            failedAttempts = 0;
        }
    }
}
