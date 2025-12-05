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
    private Map<String, Team> mapCodigos;

    // 🔥 NOVA BARREIRA: Barreira do Lobby
    private TeamBarrier lobbyBarrier;
    private final int numEquipasEsperadas;

    public GameServer(Map<String, Team> mapCodigos) {
        this.mapCodigos = mapCodigos;
        this.numEquipasEsperadas = mapCodigos.size(); // Número de equipas definido na TUI
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {

            System.out.println("🟦 Servidor à escuta na porta 12345...");

            List<Question> perguntas = QuizLoader.load("src/quizzes.json");
            gameState = new GameState(perguntas);

            // 🔥 CONFIGURAR A BARREIRA DO LOBBY (60 segundos = 60000ms)
            // Quando a barreira quebrar (todos chegaram ou timeout), executa o lambda:
            this.lobbyBarrier = new TeamBarrier(numEquipasEsperadas, 300000, () -> {
                System.out.println("🏁 LOBBY FECHADO! O jogo vai começar...");
                new GameLoop(this, gameState).start();
            });

            System.out.println("⏳ À espera de " + numEquipasEsperadas + " equipas (Timeout: 5m)...");

            while (true) {
                Socket socket = serverSocket.accept();

                // Passamos a lobbyBarrier para o Handler
                GameHandler handler = new GameHandler(socket, this, gameState, lobbyBarrier);
                clients.add(handler);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void removeClient(GameHandler handler) {
        clients.remove(handler);
        System.out.println("🗑️ Cliente removido da lista. Total: " + clients.size());
    }

    // ... Getters e Broadcast (iguais) ...
    public List<GameHandler> getClients() { return clients; }
    public Team getTeamByCode(String code) { return mapCodigos.get(code); }
    public synchronized void broadcast(Mensagem msg) {
        for (GameHandler handler : clients) handler.send(msg);
    }
}