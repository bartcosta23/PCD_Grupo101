package kahoot.Concorrencia;


public class TeamBarrier {
    private int count;                // Jogadores na equipa que faltam chegar
    private final int totalPlayers;
    private final long timeout;
    private boolean broken = false;   // Se o tempo acabou
    private final Runnable barrierAction; // Ação para calcular pontuação

    public TeamBarrier(int totalPlayers, long timeout, Runnable barrierAction) {
        this.totalPlayers = totalPlayers;
        this.count = totalPlayers;
        this.timeout = timeout;
        this.barrierAction = barrierAction;
    }

    // Chamado pelo GameHandler de cada jogador da equipa
    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeRemaining = timeout;

        count--;

        // Se chegou o último jogador (ou se a barreira já estava partida pelo tempo)
        if (count == 0 || broken) {
            // Só o último executa a lógica de pontuação (se não foi timeout)
            if (!broken && count == 0 && barrierAction != null) {
                barrierAction.run();
            }
            broken = true; // Marca como aberta para futuros atrasados
            notifyAll();   // Acorda todos os colegas de equipa
            return;
        }

        // Bloqueia à espera dos colegas
        while (count > 0 && timeRemaining > 0 && !broken) {
            wait(timeRemaining);
            timeRemaining = timeout - (System.currentTimeMillis() - startTime);
        }

        // Se acordou e o contador > 0, foi timeout
        if (count > 0) {
            broken = true;
            notifyAll(); // Acorda os outros que ainda estejam à espera
            // Aqui, segundo o enunciado, também deve correr a barrierAction
            // ou uma lógica de "falha" se necessário.
            if (barrierAction != null) {
                barrierAction.run();
            }
        }
    }
}