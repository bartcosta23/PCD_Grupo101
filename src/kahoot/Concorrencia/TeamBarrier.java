package kahoot.Concorrencia;

public class TeamBarrier {
    private int count;
    private final int totalPlayers;
    private final long timeout;
    private boolean broken = false;
    private boolean actionExecuted = false;
    private final Runnable barrierAction;

    public TeamBarrier(int totalPlayers, long timeout, Runnable barrierAction) {
        this.totalPlayers = totalPlayers;
        this.count = totalPlayers;
        this.timeout = timeout;
        this.barrierAction = barrierAction;
    }

    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeRemaining = timeout;

        count--;

        if (count == 0 || broken) {
            runActionOnce();
            broken = true;
            notifyAll();
            return;
        }

        while (count > 0 && timeRemaining > 0 && !broken) {
            wait(timeRemaining);
            timeRemaining = timeout - (System.currentTimeMillis() - startTime);
        }

        if (count > 0) {
            broken = true;
            runActionOnce();
            notifyAll();
        }
    }

    private void runActionOnce() {
        if (!actionExecuted && barrierAction != null) {
            actionExecuted = true;
            barrierAction.run();
        }
    }
}