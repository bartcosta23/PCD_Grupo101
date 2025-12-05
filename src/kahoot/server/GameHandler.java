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

    private CountDownLatch currentLatch;
    private TeamBarrier currentBarrier;
    private boolean isTeamMode = false;

    public GameHandler(Socket socket, GameServer server, GameState gameState) {
        this.socket = socket;
        this.server = server;
        this.gameState = gameState;

        try {
            // 🔥 INICIALIZA STREAMS AQUI!
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLatch(CountDownLatch latch) {
        this.currentLatch = latch;
        this.isTeamMode = false;
    }

    public void setBarrier(TeamBarrier barrier) {
        this.currentBarrier = barrier;
        this.isTeamMode = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("📥 Handler iniciado.");

            while (true) {

                Mensagem msg = (Mensagem) in.readObject();

                switch (msg.getType()) {
                    case LOGIN -> handleLogin(msg.getContent());
                    case ANSWER -> processarResposta(msg.getContent());
                    default -> System.out.println("Mensagem desconhecida: " + msg.getType());
                }
            }

        } catch (EOFException e) {
            System.out.println("🔌 Cliente desconectou.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(Object content) {
        if (content instanceof String[] dados) {
            String username = dados[0];
            this.player = new Player(username);
            System.out.println("✅ Login: " + username);
        }
    }

    private void processarResposta(Object content) {
        if (content instanceof Integer opcaoIndex) {

            Question pergunta = gameState.getPerguntaAtual();
            boolean acertou = pergunta.isCorrect(opcaoIndex);

            // 🔥 Manda o feedback AGORA!
            send(new Mensagem(
                    MessagesEnum.ANSWER_RESULT,
                    new Object[]{ opcaoIndex, acertou }
            ));

            int pontos = acertou ? 1 : 0;

            System.out.println("Jogador " + player.getUsername() +
                    " respondeu: " + opcaoIndex);

            if (!isTeamMode && currentLatch != null) {
                int bonus = currentLatch.countdown();
                if (pontos > 0)
                    gameState.adicionarPontos(player.getUsername(), pontos * bonus);
            }
            else if (isTeamMode && currentBarrier != null) {
                try { currentBarrier.await(); }
                catch (InterruptedException ignored) {}
            }
        }
    }



    public void send(Mensagem msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar mensagem ao cliente: " + e.getMessage());
        }
    }

    public Player getPlayer() { return player; }
}
