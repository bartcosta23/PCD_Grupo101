package kahoot.server;

import kahoot.Concorrencia.CountDownLatch;
import kahoot.game.GameState;
import kahoot.game.Player;
import kahoot.game.Question;
import kahoot.game.Team;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameHandler extends Thread {

    private final Socket socket;
    private final GameServer serverCentral;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameRoom salaAtual;
    private Player player;

    // 🔥 NOVA VARIÁVEL: Para saber a quem dar os pontos
    private String nomeEquipa;

    private CountDownLatch currentLatch;
    private boolean isLogged = false;
    private boolean isTeamMode = true;

    public GameHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.serverCentral = server;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setLatch(CountDownLatch latch) { this.currentLatch = latch; }
    public void setTeamMode(boolean isTeamMode) { this.isTeamMode = isTeamMode; }

    @Override
    public void run() {
        System.out.println("🧵 Handler a correr na thread: " + Thread.currentThread().getName());
        try {
            if (!isLogged) {
                Mensagem msgLogin = (Mensagem) in.readObject();
                if (msgLogin.getType() == MessagesEnum.LOGIN) {
                    if (handleLogin(msgLogin.getContent())) {
                        this.isLogged = true;
                        salaAtual.adicionarJogador(this);
                        return;
                    } else {
                        socket.close(); return;
                    }
                }
            }

            if (salaAtual != null && salaAtual.getLobbyBarrier() != null) {
                System.out.println("⏳ [" + salaAtual.getId() + "] " + player.getUsername() + " à espera...");
                salaAtual.getLobbyBarrier().await();
            }

            while (true) {
                Mensagem msg = (Mensagem) in.readObject();
                if (msg.getType() == MessagesEnum.ANSWER) {
                    processarResposta(msg.getContent());
                }
            }
        } catch (Exception e) {
            System.out.println("🔌 Cliente caiu.");
            if (salaAtual != null) salaAtual.removerJogador(this);
        }
    }

    private boolean handleLogin(Object content) {
        if (content instanceof String[] dados) {
            String username = dados[0];
            String codigo = dados[1];
            GameRoom salaEncontrada = serverCentral.descobrirSala(codigo);

            if (salaEncontrada != null) {
                this.salaAtual = salaEncontrada;
                this.player = new Player(username);

                // 🔥 GUARDAR O NOME DA EQUIPA
                Team t = salaAtual.getTeamByCode(codigo);
                this.nomeEquipa = t.getNome();

                send(new Mensagem(MessagesEnum.LOGIN, "OK"));
                return true;
            } else {
                send(new Mensagem(MessagesEnum.LOGIN, "ERRO"));
                return false;
            }
        }
        return false;
    }

    private void processarResposta(Object content) {
        if (content instanceof Integer opcaoIndex) {
            GameState gameState = salaAtual.getGameState();
            Question pergunta = gameState.getPerguntaAtual();

            boolean acertou = pergunta.isCorrect(opcaoIndex);
            send(new Mensagem(MessagesEnum.ANSWER_RESULT, new Object[]{opcaoIndex, acertou}));

            int pontosBase = acertou ? 1 : 0;
            int pontosFinais = 0;

            if (currentLatch != null) {
                if (isTeamMode) {
                    // Modo Equipa: 1 ponto fixo
                    currentLatch.countdown(); // (minúsculo, corrigido)
                    pontosFinais = pontosBase;
                } else {
                    // Modo Individual: Bónus de rapidez
                    int multiplicador = currentLatch.countdown();
                    pontosFinais = pontosBase * multiplicador;
                }

                if (pontosFinais > 0) {
                    // 🔥 CORREÇÃO: Adiciona pontos à EQUIPA, não ao jogador!
                    // Como o GameState usa um Map, ele vai somar automaticamente se a chave for igual.
                    gameState.adicionarPontos(this.nomeEquipa, pontosFinais);

                    System.out.println("💰 Pontos atribuídos à " + this.nomeEquipa + ": " + pontosFinais);
                }
            }
        }
    }

    public void send(Mensagem msg) {
        try { out.writeObject(msg); out.flush(); } catch (IOException e) {}
    }
}