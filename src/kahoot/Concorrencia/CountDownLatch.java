package kahoot.Concorrencia;

public class CountDownLatch {
    private final int bonusFactor;
    private int bonusCount;
    private final long waitPeriod;
    private int count;
    private boolean timeExpired = false;

    public CountDownLatch(int bonusFactor, int bonusCount, int waitPeriod, int totalPlayers) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        this.count = totalPlayers;
    }

    public synchronized int countdown() {
        if (timeExpired || count <= 0) {
            return 1;
        }

        count--;
        int currentBonus = 1;

        if (bonusCount > 0) {
            currentBonus = bonusFactor;
            bonusCount--;
        }

        if (count == 0) {
            notifyAll();
        }

        return currentBonus;
    }

    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeRemaining = waitPeriod;

        while (count > 0 && timeRemaining > 0) {
            wait(timeRemaining);
            long timeElapsed = System.currentTimeMillis() - startTime;
            timeRemaining = waitPeriod - timeElapsed;
        }

        if (count > 0) {
            timeExpired = true;
        }
    }
}