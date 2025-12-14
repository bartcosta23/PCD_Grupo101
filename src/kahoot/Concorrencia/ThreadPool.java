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

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    // Método para submeter uma tarefa (ex: um GameLoop)
    public void execute(Runnable task) {
        synchronized (queue) {
            if (isRunning) {
                queue.addLast(task);
                queue.notify();
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    private class PoolWorker extends Thread {
        @Override
        public void run() {
            Runnable task;

            while (isRunning) {
                synchronized (queue) {
                    while (queue.isEmpty() && isRunning) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (!isRunning && queue.isEmpty()) {
                        return;
                    }

                    task = queue.removeFirst();
                }

                try {
                    task.run();
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