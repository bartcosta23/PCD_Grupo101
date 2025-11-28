package kahoot.server;

import kahoot.messages.*;
import kahoot.game.*;
import kahoot.Concorrencia.*;
import java.io.*;
import java.net.Socket;

public class GameHandler extends Thread {

    private Socket socket;
    private GameServer server;
    private GameState gameState;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Player player;

    // Variáveis de Concorrência
    private CountDownLatch currentLatch;
    private TeamBarrier currentBarrier;
    private boolean isTeamMode = false;

    public GameHandler(Socket socket, GameServer server, GameState gameState) {
        this.socket = socket;
        this.server = server;
        this.gameState = gameState;
    }

    // (Mantém os métodos setLatch e setBarrier iguais aos anteriores...)
    public void setLatch(CountDownLatch latch) { this.currentLatch = latch; this.isTeamMode = false; }
    public void setBarrier(TeamBarrier barrier) { this.currentBarrier = barrier; this.isTeamMode = true; }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            System.out.println("📥 Handler iniciado.");

            while (true) {
                // 1. Lê a mensagem genérica
                Mensagem msg = (Mensagem) in.readObject();

                // 2. Decide o que fazer com base no ENUM
                switch (msg.getType()) {
                    case LOGIN:
                        handleLogin(msg.getContent());
                        break;

                    case ANSWER:
                        processarResposta(msg.getContent());
                        break;

                    default:
                        System.out.println("Mensagem desconhecida: " + msg.getType());
                }
            }
        } catch (EOFException e) {
            System.out.println("🔌 Cliente saiu.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS AUXILIARES PARA TRATAR O CONTEÚDO ---

    private void handleLogin(Object content) {
        // Assume que o LOGIN envia um array de Strings: [username, teamName]
        if (content instanceof String[] dados) {
            String username = dados[0];
            String teamName = dados.length > 1 ? dados[1] : null;

            this.player = new Player(username);
            // Lógica de adicionar à equipa aqui...
            System.out.println("✅ Login: " + username);
        }
    }

    private void processarResposta(Object content) { // Renomeado de handleAnswer
        if (content instanceof Integer opcaoIndex) {

            Question perguntaAtual = gameState.getPerguntaAtual();
            boolean acertou = perguntaAtual.isCorrect(opcaoIndex);
            int pontosBase = acertou ? 1 : 0;

            System.out.println("Jogador " + player.getUsername() + " respondeu: " + opcaoIndex);

            // --- LÓGICA DE CONCORRÊNCIA (Igual à anterior) ---
            if (!isTeamMode && currentLatch != null) {
                int bonus = currentLatch.countdown();
                if (pontosBase > 0) gameState.adicionarPontos(player.getUsername(), pontosBase * bonus);
            }
            else if (isTeamMode && currentBarrier != null) {
                try {
                    currentBarrier.await();
                } catch (InterruptedException e) { }
            }
        }
    }

    public void enviar(Mensagem msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) { }
    }

    public Player getPlayer() { return player; }
}