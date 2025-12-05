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

    // Concorrência
    private TeamBarrier lobbyBarrier; // 🔥 Barreira recebida do server
    private CountDownLatch currentLatch;
    private TeamBarrier currentBarrier;
    private boolean isTeamMode = false;

    // 🔥 Construtor atualizado para receber lobbyBarrier
    public GameHandler(Socket socket, GameServer server, GameState gameState, TeamBarrier lobbyBarrier) {
        this.socket = socket;
        this.server = server;
        this.gameState = gameState;
        this.lobbyBarrier = lobbyBarrier; // Guardar referência

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Setters mantêm-se iguais...
    public void setLatch(CountDownLatch latch) { this.currentLatch = latch; this.isTeamMode = false; }
    public void setBarrier(TeamBarrier barrier) { this.currentBarrier = barrier; this.isTeamMode = true; }

    @Override
    public void run() {
        try {
            System.out.println("📥 Handler iniciado.");

            // 1. Ler Login
            Mensagem msgLogin = (Mensagem) in.readObject();

            // 2. 🔥 VALIDAR LOGIN (Se falhar, fecha a thread e sai)
            if (msgLogin.getType() == MessagesEnum.LOGIN) {
                boolean loginSucesso = handleLogin(msgLogin.getContent());
                if (!loginSucesso) {
                    System.out.println("⛔ Cliente rejeitado. A fechar conexão.");
                    server.removeClient(this);
                    socket.close();
                    return; // SAI DO MÉTODO RUN IMEDIATAMENTE! Não vai para a barreira.
                }
            }

            // 3. Esperar na Barreira (Só chega aqui se o código estiver certo)
            if (lobbyBarrier != null) {
                System.out.println("⏳ Jogador " + player.getUsername() + " aceite. A aguardar equipas...");
                lobbyBarrier.await();
            }

            // 4. Loop do Jogo
            while (true) {
                Mensagem msg = (Mensagem) in.readObject();
                // ... (switch case ANSWER, etc) ...
                if (msg.getType() == MessagesEnum.ANSWER) {
                    processarResposta(msg.getContent());
                }
            }

        } catch (Exception e) {
            // e.printStackTrace(); // Comentar para não sujar log quando cliente sai
            System.out.println("🔌 Conexão encerrada.");
        }
    }

    // 🔥 AGORA RETORNA BOOLEAN E ENVIA RESPOSTA AO CLIENTE
    private boolean handleLogin(Object content) {
        if (content instanceof String[] dados) {
            String username = dados[0];
            String codigo = dados[1];

            Team equipa = server.getTeamByCode(codigo);

            if (equipa != null) {
                this.player = new Player(username);
                System.out.println("✅ Login Válido: " + username + " -> " + equipa.getNome());

                // Envia confirmação ao cliente
                send(new Mensagem(MessagesEnum.LOGIN, "OK"));
                return true;
            } else {
                System.out.println("❌ Login Inválido: Código " + codigo + " não existe.");

                // Envia erro ao cliente
                send(new Mensagem(MessagesEnum.LOGIN, "ERRO"));
                return false;
            }
        }
        return false;
    }

    // ... Resto dos métodos (processarResposta, send) iguais ...
    private void processarResposta(Object content) {
        if (content instanceof Integer opcaoIndex) {
            Question pergunta = gameState.getPerguntaAtual();
            boolean acertou = pergunta.isCorrect(opcaoIndex);
            send(new Mensagem(MessagesEnum.ANSWER_RESULT, new Object[]{ opcaoIndex, acertou }));

            int pontos = acertou ? 1 : 0;
            // Lógica de Latch/Barrier de jogo (NÃO confundir com a do Lobby)
            if (!isTeamMode && currentLatch != null) {
                int bonus = currentLatch.countdown();
                if (pontos > 0) gameState.adicionarPontos(player.getUsername(), pontos * bonus);
            } else if (isTeamMode && currentBarrier != null) {
                try { currentBarrier.await(); } catch (InterruptedException ignored) {}
            }
        }
    }
    public void send(Mensagem msg) { try { out.writeObject(msg); out.flush(); } catch (Exception e) {} }
}