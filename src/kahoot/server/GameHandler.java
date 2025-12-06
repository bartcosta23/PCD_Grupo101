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
    private TeamBarrier lobbyBarrier;
    private CountDownLatch currentLatch;
    private TeamBarrier currentBarrier;
    private boolean isTeamMode = false;

    public GameHandler(Socket socket, GameServer server, GameState gameState, TeamBarrier lobbyBarrier) {
        this.socket = socket;
        this.server = server;
        this.gameState = gameState;
        this.lobbyBarrier = lobbyBarrier;

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- SETTERS DE CONCORRÊNCIA ---

    // Chamado pelo GameLoop antes de enviar a pergunta
    public void setLatch(CountDownLatch latch) {
        this.currentLatch = latch;
        // Nota: O latch é usado nos dois modos para avisar o servidor que a thread acabou!
    }

    public void setBarrier(TeamBarrier barrier) {
        this.currentBarrier = barrier;
        // Se receber barreira, ativa o modo equipa
        this.isTeamMode = (barrier != null);
    }

    @Override
    public void run() {
        try {
            System.out.println("📥 Handler iniciado na thread " + this.getId());

            // 1. Ler e Validar Login
            Mensagem msgLogin = (Mensagem) in.readObject();

            if (msgLogin.getType() == MessagesEnum.LOGIN) {
                if (!handleLogin(msgLogin.getContent())) {
                    server.removeClient(this);
                    socket.close();
                    return;
                }
            }

            // 2. Esperar na Barreira do Lobby (Início do Jogo)
            if (lobbyBarrier != null) {
                lobbyBarrier.await();
            }

            // 3. Loop Principal do Jogo
            while (true) {
                try {
                    Mensagem msg = (Mensagem) in.readObject();

                    if (msg.getType() == MessagesEnum.ANSWER) {
                        processarResposta(msg.getContent());
                    }
                } catch (EOFException | java.net.SocketException e) {
                    System.out.println("🔌 Cliente desconectou-se.");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handleLogin(Object content) {
        if (content instanceof String[] dados) {
            String username = dados[0];
            String codigo = dados[1];
            Team equipa = server.getTeamByCode(codigo);

            if (equipa != null && equipa.addPlayer(new Player(username))) {
                // Recupera a referência real do jogador que foi adicionado à equipa
                this.player = equipa.getMembers().get(equipa.getMembers().size() - 1);

                System.out.println("✅ Login: " + username + " na equipa " + equipa.getNome());
                send(new Mensagem(MessagesEnum.LOGIN, "OK"));
                return true;
            } else {
                send(new Mensagem(MessagesEnum.LOGIN, "ERRO: Equipa cheia ou código inválido"));
                return false;
            }
        }
        return false;
    }

    // 🔥 O MÉTODO CRÍTICO QUE FOI CORRIGIDO
    private void processarResposta(Object content) {
        if (content instanceof Integer opcaoIndex) {
            Question pergunta = gameState.getPerguntaAtual();

            // 1. Guardar a resposta no Jogador (CRUCIAL para o Modo Equipa)
            // Tens de adicionar setLastAnswer(int) na classe Player!
            player.setLastAnswer(opcaoIndex);

            // 2. Enviar feedback imediato (acertou/falhou)
            boolean acertou = pergunta.isCorrect(opcaoIndex);
            send(new Mensagem(MessagesEnum.ANSWER_RESULT, new Object[]{ opcaoIndex, acertou }));

            // 3. Sincronização e Pontuação
            if (isTeamMode) {
                // === MODO EQUIPA ===
                if (currentBarrier != null) {
                    try {
                        // Espera pelo parceiro.
                        // Quando sair daqui, a BarrierAction (no GameLoop) JÁ calculou os pontos!
                        currentBarrier.await();
                    } catch (InterruptedException e) {
                        System.out.println("⚠️ Erro na barreira de equipa");
                    }
                }

                // AVISAR O SERVIDOR QUE ACABEI
                // Mesmo em equipa, temos de avisar o GameLoop para não ficar bloqueado
                if (currentLatch != null) {
                    currentLatch.countdown();
                }

            } else {
                // === MODO INDIVIDUAL ===
                if (currentLatch != null) {
                    // countdown() retorna o bónus (2 ou 1)
                    int bonus = currentLatch.countdown();

                    if (acertou) {
                        int pontos = pergunta.getPoints() * bonus;
                        gameState.adicionarPontos(player.getUsername(), pontos);
                        System.out.println("💰 Pontos para " + player.getUsername() + ": " + pontos);
                    }
                }
            }
        }
    }

    public void send(Mensagem msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}