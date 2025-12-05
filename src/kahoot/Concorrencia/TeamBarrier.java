package kahoot.Concorrencia;

public class TeamBarrier {
    private int count;
    private final int totalPlayers;
    private final long timeout;
    private boolean broken = false;
    private boolean actionExecuted = false; // 🔥 NOVO: Evita iniciar o jogo várias vezes
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

        // Se é o último a chegar OU a barreira já partiu (tempo acabou)
        if (count == 0 || broken) {
            runActionOnce(); // Executa a ação de forma segura
            broken = true;
            notifyAll();
            return;
        }

        // Bloqueia à espera
        while (count > 0 && timeRemaining > 0 && !broken) {
            wait(timeRemaining);
            timeRemaining = timeout - (System.currentTimeMillis() - startTime);
        }

        // Se saiu do wait: ou chegou toda a gente, ou foi TIMEOUT
        if (count > 0) {
            broken = true;
            runActionOnce(); // Executa a ação de forma segura no timeout
            notifyAll();
        }
    }

    // 🔥 Garante que o GameLoop só arranca uma vez
    private void runActionOnce() {
        if (!actionExecuted && barrierAction != null) {
            actionExecuted = true;
            barrierAction.run();
        }
    }
}