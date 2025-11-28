package kahoot.server;

import kahoot.game.GameState;
import kahoot.game.Question;
import kahoot.messages.Mensagem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public static final int PORT = 5001;

    private final List<GameHandler> clientes = new ArrayList<>();

    // O Estado do Jogo (Tem de existir para ser passado aos Handlers)
    private GameState gameState;

    public GameServer() {
        // --- INICIALIZAÇÃO TEMPORÁRIA (Para teste) ---
        // Mais tarde, isto será substituído pela leitura do ficheiro JSON [cite: 61]
        List<Question> perguntas = new ArrayList<>();
        List<String> opcoes = new ArrayList<>();
        opcoes.add("Opção A"); opcoes.add("Opção B");
        opcoes.add("Opção C"); opcoes.add("Opção D");

        // Pergunta de teste: "Teste?", Resposta correta: índice 1 (Opção B), 10 pontos
        perguntas.add(new Question("Pergunta de Teste?", opcoes, 1, 10));

        this.gameState = new GameState(perguntas);
    }

    public void startServer() {

        System.out.println(" Servidor Kahoot iniciado na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                System.out.println(" A aguardar clientes...");
                Socket socket = serverSocket.accept();
                System.out.println(" Cliente ligado: " + socket.getInetAddress());

                // CORREÇÃO 1: Passar o 'gameState' para o Handler
                // O Handler precisa disto para validar respostas
                GameHandler handler = new GameHandler(socket, this, gameState);

                synchronized (clientes) {
                    clientes.add(handler);
                }

                handler.start();
            }

        } catch (IOException e) {
            System.err.println(" Erro no servidor: " + e.getMessage());
        }
    }

    // CORREÇÃO 2: O broadcast deve receber uma 'Mensagem' e reenviá-la
    public void broadcast(Mensagem msg) {
        synchronized (clientes) {
            for (GameHandler h : clientes) {
                h.enviar(msg); // Envia a mensagem que recebeu, não uma nova vazia
            }
        }
    }

    public List<GameHandler> getClientes() {
        return clientes;
    }

    public static void main(String[] args) {
        new GameServer().startServer();
    }
}