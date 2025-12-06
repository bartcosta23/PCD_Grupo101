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
        // Assume-se que getClients() devolve a referência para a lista viva
        this.clientes = server.getClients();
    }

    @Override
    public void run() {
        System.out.println("🎮 GameLoop iniciado.");

        // Pequena pausa inicial para todos se prepararem
        esperar(2000);

        while (true) {
            // 1 ▬▬▬ Verificar se há perguntas
            if (!gameState.temPerguntaAtual()) {
                break;
            }

            // 2 ▬▬▬ Enviar pergunta
            Question q = gameState.getPerguntaAtual();
            System.out.println("📤 Enviando pergunta: " + q.getText());
            server.broadcast(new Mensagem(MessagesEnum.QUESTION, q));

            // 3 ▬▬▬ Sincronização (Respostas)
            try {
                // Definição do Latch:
                // Fator Bonus: 2 (multiplicador)
                // Quem recebe bonus: Math.min(3, size) -> Só os 3 primeiros ganham extra!
                // Timeout: 15s
                // Total jogadores: size
                int totalJogadores = clientes.size();
                int numBonus = Math.min(3, totalJogadores); // Ex: Só top 3 ganha bonus

                CountDownLatch latch = new CountDownLatch(2, numBonus, 15000, totalJogadores);

                // Passar o latch a todos os handlers ativos
                // Nota: É importante fazer isto num bloco synchronized se a lista puder mudar
                synchronized (clientes) {
                    for (GameHandler handler : clientes) {
                        handler.setLatch(latch);
                    }
                }

                System.out.println("⏳ À espera de respostas...");
                latch.await(); // Bloqueia aqui até todos responderem ou timeout

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 4 ▬▬▬ Enviar placar
            System.out.println("📊 A enviar classificações...");
            server.broadcast(new Mensagem(MessagesEnum.SCORE, gameState.getPlacar()));

            // 🔥 CORREÇÃO: Esperar 5 segundos para os alunos verem os pontos!
            esperar(2000);

            // 5 ▬▬▬ Avançar
            if (!gameState.proximaPergunta()) {
                System.out.println("🏁 Perguntas acabaram.");
                break;
            }
        }

        // 6 ▬▬▬ FIM DO JOGO
        System.out.println("🏆 Jogo Terminado. A notificar clientes.");

        // (Opcional) Podes criar um tipo MessagesEnum.GAME_OVER
        // Ou enviar o Score final uma última vez com uma flag especial
        // server.broadcast(new Mensagem(MessagesEnum.GAME_OVER, "Fim!"));

        System.out.println("🏁 Thread GameLoop fechada.");
    }

    // Método auxiliar para não encher o código de try-catch
    private void esperar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}