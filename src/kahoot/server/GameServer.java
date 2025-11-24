package kahoot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public static final int PORT = 5001;

    private final List<GameHandler> clientes = new ArrayList<>();

    public void startServer() {

        System.out.println(" Servidor Kahoot iniciado na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {

                System.out.println(" A aguardar clientes...");
                Socket socket = serverSocket.accept();
                System.out.println(" Cliente ligado: " + socket.getInetAddress());

                // Criar handler para o cliente
                GameHandler handler = new GameHandler(socket, this);

                // Guardar o cliente
                synchronized (clientes) {
                    clientes.add(handler);
                }

                // Iniciar thread
                handler.start();
            }

        } catch (IOException e) {
            System.err.println(" Erro no servidor: " + e.getMessage());
        }
    }

    // Broadcast para todos os clientes
    public void broadcast(Object msg) {

        synchronized (clientes) {
            for (GameHandler h : clientes) {
                h.enviar(msg);
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

