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
        System.out.println("🎮 GameLoop iniciado na sala: " + gameRoom.getId());
        esperar(2000);

        int numeroRonda = 1; // Contador para alternar modos

        while (true) {
            if (!gameState.temPerguntaAtual()) break;

            Question q = gameState.getPerguntaAtual();

            // 🔥 LÓGICA DE ALTERNÂNCIA
            // Se a ronda for ÍMPAR (1, 3, 5) -> isTeamMode = true (Modo Equipa)
            // Se a ronda for PAR (2, 4, 6)   -> isTeamMode = false (Modo Individual/Rápido)
            boolean isTeamMode = (numeroRonda % 2 != 0);

            System.out.println("📤 [" + gameRoom.getId() + "] Pergunta " + numeroRonda + " (" + (isTeamMode ? "Equipa" : "Individual") + "): " + q.getText());

            // Envia Pergunta + Modo para a GUI atualizar as cores
            gameRoom.broadcast(new Mensagem(MessagesEnum.QUESTION, new Object[]{q, isTeamMode}));

            try {
                int total = clientes.size();
                int bonus = Math.min(3, total);

                // Latch configurado (15 segundos)
                CountDownLatch latch = new CountDownLatch(2, bonus, 15000, total);

                synchronized (clientes) {
                    for (GameHandler handler : clientes) {
                        handler.setLatch(latch);
                        // 🔥 AVISAR O HANDLER DO MODO ATUAL
                        // Isto é importante para ele saber se dá bónus de rapidez ou não
                        handler.setTeamMode(isTeamMode);
                    }
                }
                latch.await();

            } catch (InterruptedException e) { e.printStackTrace(); }

            gameRoom.broadcast(new Mensagem(MessagesEnum.SCORE, gameState.getPlacar()));
            esperar(1500);

            if (!gameState.proximaPergunta()) break;

            numeroRonda++; // Avança para a próxima ronda
        }

        System.out.println("🏆 Jogo Terminado na sala " + gameRoom.getId());
        gameRoom.broadcast(new Mensagem(MessagesEnum.GAME_OVER, gameState.getPlacar()));
    }

    private void esperar(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}