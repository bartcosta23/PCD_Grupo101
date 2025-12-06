package kahoot.server;

import kahoot.game.*;
import kahoot.messages.Mensagem;
import kahoot.Concorrencia.TeamBarrier;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameServer {

    private final List<GameHandler> clients = new ArrayList<>();
    private GameState gameState;
    private final Map<String, Team> mapCodigos;

    // 🔥 BARREIRA DO LOBBY: Espera que todos entrem antes de começar
    private TeamBarrier lobbyBarrier;

    // Variáveis de Configuração
    private final int totalJogadoresEsperados;

    public GameServer(Map<String, Team> mapCodigos) {
        this.mapCodigos = mapCodigos;

        // 🔥 CUMPRIMENTO DO ENUNCIADO: Equipas de 2 jogadores.
        // O jogo só começa quando as cadeiras estiverem todas cheias.
        // Ex: 3 Equipas * 2 Jogadores = Espera por 6 conexões.
        this.totalJogadoresEsperados = mapCodigos.size() * 2;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {

            System.out.println("🟦 Servidor à escuta na porta 12345...");

            // 1. Carregar Perguntas e Criar Estado do Jogo
            List<Question> perguntas = QuizLoader.load("src/quizzes.json");
            gameState = new GameState(perguntas);

            // 2. Configurar a Barreira do Lobby
            // Timeout de 5 minutos (300000ms) para todos fazerem login.
            // Quando a barreira quebrar (todos chegaram), arranca o GameLoop.
            this.lobbyBarrier = new TeamBarrier(totalJogadoresEsperados, 300000, () -> {
                System.out.println("🏁 LOBBY FECHADO! Todos os " + totalJogadoresEsperados + " jogadores entraram.");
                System.out.println("🎮 A iniciar GameLoop...");
                new GameLoop(this, gameState).start();
            });

            System.out.println("⏳ À espera de " + totalJogadoresEsperados + " jogadores no total (Timeout: 5m)...");

            // 3. Loop de aceitação de clientes
            while (true) {
                Socket socket = serverSocket.accept();

                // Passamos a lobbyBarrier para o Handler.
                // O Handler vai ficar bloqueado no "await()" desta barreira até entrarem todos.
                GameHandler handler = new GameHandler(socket, this, gameState, lobbyBarrier);

                synchronized (this) {
                    clients.add(handler);
                }

                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS DE GESTÃO DE CLIENTES ---

    public synchronized void removeClient(GameHandler handler) {
        clients.remove(handler);
        System.out.println("🗑️ Cliente removido da lista. Total ativos: " + clients.size());
    }

    public synchronized List<GameHandler> getClients() {
        return new ArrayList<>(clients); // Retorna cópia para evitar erros de concorrência
    }

    public Team getTeamByCode(String code) {
        return mapCodigos.get(code);
    }

    public synchronized void broadcast(Mensagem msg) {
        for (GameHandler handler : clients) {
            handler.send(msg);
        }
    }

    // Método necessário para o GameLoop criar as barreiras das equipas
    public synchronized List<Team> getTeams() {
        return new ArrayList<>(mapCodigos.values());
    }
}