package kahoot.gui;

import javax.swing.SwingUtilities;

public class GameTimer extends Thread {

    private int segundosRestantes;
    private final Runnable onTick;   // O que fazer a cada segundo
    private final Runnable onFinish; // O que fazer quando acaba
    private boolean running = true;  // Controlo para parar a thread

    public GameTimer(int segundos, Runnable onTick, Runnable onFinish) {
        this.segundosRestantes = segundos;
        this.onTick = onTick;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        while (running && segundosRestantes > 0) {
            try {
                // Espera 1 segundo
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Se for interrompido, sai do loop
                break;
            }

            if (!running) break; // Verifica se foi cancelado durante o sono

            segundosRestantes--;

            //  IMPORTANTE: Como estamos numa Thread separada,
            // não podemos mexer na GUI diretamente. Usamos invokeLater.
            SwingUtilities.invokeLater(onTick);
        }

        // Se o tempo acabou naturalmente (e não foi cancelado manualmente)
        if (running && segundosRestantes <= 0) {
            SwingUtilities.invokeLater(onFinish);
        }
    }

    // Método para parar o timer (quando o aluno responde antes do tempo)
    public void parar() {
        this.running = false;
        this.interrupt(); // Acorda a thread se estiver a dormir
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }
}