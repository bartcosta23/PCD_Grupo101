package kahoot.gui;

import javax.swing.SwingUtilities;

public class GameTimer extends Thread {

    private int segundosRestantes;
    private final Runnable onTick;
    private final Runnable onFinish;
    private boolean running = true;

    public GameTimer(int segundos, Runnable onTick, Runnable onFinish) {
        this.segundosRestantes = segundos;
        this.onTick = onTick;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        while (running && segundosRestantes > 0) {
            try {
                // 1 segundo
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }

            if (!running) break;

            segundosRestantes--;


            SwingUtilities.invokeLater(onTick);
        }

        if (running && segundosRestantes <= 0) {
            SwingUtilities.invokeLater(onFinish);
        }
    }

    public void parar() {
        this.running = false;
        this.interrupt();
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }
}