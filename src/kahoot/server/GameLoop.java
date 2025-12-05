package kahoot.server;

import kahoot.Concorrencia.CountDownLatch;
import kahoot.game.GameState;
import kahoot.game.Question;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import java.util.List;

public class GameLoop extends Thread {

    private final GameServer server;
    private final GameState gameState;
    private final List<GameHandler> clientes;

    public GameLoop(GameServer server, GameState gameState) {
        this.server = server;
        this.gameState = gameState;
        this.clientes = server.getClients();
    }

    @Override
    public void run() {
        System.out.println("🎮 GameLoop iniciado.");

        while (true) {

            // 1 ▬▬▬ Não há mais perguntas → terminar
            if (!gameState.temPerguntaAtual()) {
                System.out.println("🏁 Sem mais perguntas. Jogo acabou.");
                break;
            }

            // 2 ▬▬▬ Enviar pergunta
            Question q = gameState.getPerguntaAtual();
            System.out.println("📤 Enviando pergunta: " + q.getText());

            server.broadcast(new Mensagem(MessagesEnum.QUESTION, q));


            // 3 ▬▬▬ Esperar respostas
            try {
                CountDownLatch latch = new CountDownLatch(
                        2,                      // fator bônus
                        clientes.size(),        // quantos recebem bónus
                        15000,                  // tempo máximo (15s)
                        clientes.size()         // total de jogadores
                );

                // Cada Handler precisa do latch
                for (GameHandler handler : clientes) {
                    handler.setLatch(latch);
                }

                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 4 ▬▬▬ Enviar placar
            server.broadcast(new Mensagem(
                    MessagesEnum.SCORE,
                    gameState.getPlacar()
            ));

            // 5 ▬▬▬ Passar para a próxima
            if (!gameState.proximaPergunta()) {
                System.out.println("🏁 Última pergunta terminada.");
                break;
            }
        }

        System.out.println("🏁 GameLoop terminou.");
    }
}
