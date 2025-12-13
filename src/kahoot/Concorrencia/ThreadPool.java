package kahoot.Concorrencia;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {

    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private volatile boolean isRunning = true;

    public ThreadPool(int nThreads) {
        this.nThreads = nThreads;
        this.queue = new LinkedList<>();
        this.threads = new PoolWorker[nThreads];

        // Criar e arrancar os trabalhadores (eles ficam logo à espera de trabalho)
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    // Método para submeter uma tarefa (ex: um GameLoop)
    public void execute(Runnable task) {
        synchronized (queue) {
            if (isRunning) {
                queue.addLast(task); // Adiciona à fila
                queue.notify();      // Acorda UM trabalhador que esteja a dormir
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        synchronized (queue) {
            queue.notifyAll(); // Acorda todos para eles poderem sair do loop e morrer
        }
    }

    // Classe interna que define o comportamento do Trabalhador
    private class PoolWorker extends Thread {
        @Override
        public void run() {
            Runnable task;

            while (isRunning) {
                // 1. Tentar arranjar trabalho
                synchronized (queue) {
                    // Enquanto não houver trabalho, dorme
                    while (queue.isEmpty() && isRunning) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            // Se for interrompido, verifica se deve terminar
                        }
                    }

                    // Se o servidor estiver a fechar e não houver tarefas, sai
                    if (!isRunning && queue.isEmpty()) {
                        return;
                    }

                    // Pega na primeira tarefa da fila
                    task = queue.removeFirst();
                }

                // 2. Executar o trabalho (fora do bloco synchronized para não bloquear a fila)
                try {
                    task.run(); // Executa o GameLoop nesta thread
                } catch (RuntimeException e) {
                    System.err.println("Erro na execução da tarefa: " + e.getMessage());
                }
            }
        }
    }

    // Método auxiliar apenas para debug (opcional)
    public int getWaitingTasks() {
        synchronized (queue) {
            return queue.size();
        }
    }
}