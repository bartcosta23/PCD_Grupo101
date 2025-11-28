package kahoot.Concorrencia;

public class CountDownLatch {
    private final int bonusFactor;    // Ex: 2 (duplica a pontuação)
    private int bonusCount;           // Quantos jogadores ainda recebem bónus
    private final long waitPeriod;    // Tempo limite em milissegundos
    private int count;                // Quantos jogadores faltam responder
    private boolean timeExpired = false;

    public CountDownLatch(int bonusFactor, int bonusCount, int waitPeriod, int totalPlayers) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        this.count = totalPlayers;
    }

    // Chamado pelas threads dos jogadores (GameHandler) quando recebem resposta
    public synchronized int countdown() {
        if (timeExpired || count <= 0) {
            return 1; // Sem bónus se acabou o tempo ou já responderam todos
        }

        count--;
        int currentBonus = 1;

        // Verifica se ainda há bónus disponíveis
        if (bonusCount > 0) {
            currentBonus = bonusFactor;
            bonusCount--;
        }

        // Se foi o último a responder, acorda a thread principal do jogo
        if (count == 0) {
            notifyAll();
        }

        return currentBonus;
    }

    // Chamado pela thread principal do Jogo (GameLoop) para esperar pelo fim da ronda
    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeRemaining = waitPeriod;

        // Bloqueia enquanto houver jogadores por responder E houver tempo
        while (count > 0 && timeRemaining > 0) {
            wait(timeRemaining);
            long timeElapsed = System.currentTimeMillis() - startTime;
            timeRemaining = waitPeriod - timeElapsed;
        }

        // Se saiu do loop porque o tempo acabou
        if (count > 0) {
            timeExpired = true;
        }
    }
}