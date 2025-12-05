package kahoot.server;

import kahoot.game.*;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private final List<GameHandler> clients = new ArrayList<>();
    private GameState gameState;

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {

            System.out.println("🟦 Servidor à escuta na porta 12345...");

            // 1 ▬▬▬ Carregar perguntas JSON
            List<Question> perguntas =
                    QuizLoader.load("src/quizzes.json");

            if (perguntas.isEmpty()) {
                System.err.println("❌ ERRO: Nenhuma pergunta carregada do JSON.");
                return;
            }

            gameState = new GameState(perguntas);

            // 2 ▬▬▬ Aguardar clientes
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🟢 Cliente ligado: " + socket.getInetAddress());

                GameHandler handler = new GameHandler(socket, this, gameState);
                clients.add(handler);
                handler.start();

                // 3 ▬▬▬ Primeiro cliente → iniciar jogo
                if (clients.size() == 1) {
                    System.out.println("🚀 Primeiro cliente ligado. Iniciando GameLoop...");
                    new GameLoop(this, gameState).start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ▬▬▬ usado pelo GameLoop
    public List<GameHandler> getClients() {
        return clients;
    }

    // ▬▬▬ enviar msg a todos
    public synchronized void broadcast(Mensagem msg) {
        for (GameHandler handler : clients) {
            handler.send(msg);
        }
    }
    public static void main(String[] args) {
        new GameServer().startServer();
    }

}
