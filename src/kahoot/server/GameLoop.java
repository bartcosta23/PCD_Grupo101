package kahoot.server;

import kahoot.Concorrencia.CountDownLatch;
import kahoot.game.GameState;
import kahoot.game.Question;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import java.util.List;

public class GameLoop extends Thread {

    private final GameRoom gameRoom;
    private final GameState gameState;
    private final List<GameHandler> clientes;

    public GameLoop(GameRoom gameRoom, GameState gameState) {
        this.gameRoom = gameRoom;
        this.gameState = gameState;
        this.clientes = gameRoom.getClients();
    }

    @Override
    public void run() {
        System.out.println(" GameLoop iniciado na sala: " + gameRoom.getId());
        esperar(2000);

        int numeroRonda = 1;

        while (true) {
            if (!gameState.temPerguntaAtual()) break;

            Question q = gameState.getPerguntaAtual();


            // se a ronda for ÍMPAR -> isTeamMode = true (Modo Equipa)
            // se a ronda for PAR -> isTeamMode = false (Modo Individual/Rápido)
            boolean isTeamMode = (numeroRonda % 2 != 0);

            System.out.println(" [" + gameRoom.getId() + "] Pergunta " + numeroRonda + " (" + (isTeamMode ? "Equipa" : "Individual") + "): " + q.getText());

            gameRoom.broadcast(new Mensagem(MessagesEnum.QUESTION, new Object[]{q, isTeamMode}));

            try {
                int total = clientes.size();
                int bonus = Math.min(3, total);

                CountDownLatch latch = new CountDownLatch(2, bonus, 15000, total);

                synchronized (clientes) {
                    for (GameHandler handler : clientes) {
                        handler.setLatch(latch);

                        handler.setTeamMode(isTeamMode);
                    }
                }
                latch.await();

            } catch (InterruptedException e) { e.printStackTrace(); }

            gameRoom.broadcast(new Mensagem(MessagesEnum.SCORE, gameState.getPlacar()));
            esperar(1500);

            if (!gameState.proximaPergunta()) break;

            numeroRonda++;
        }

        System.out.println(" Jogo Terminado na sala " + gameRoom.getId());
        gameRoom.broadcast(new Mensagem(MessagesEnum.GAME_OVER, gameState.getPlacar()));
    }

    private void esperar(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}