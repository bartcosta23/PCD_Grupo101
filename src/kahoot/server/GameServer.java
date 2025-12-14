package kahoot.server;

import kahoot.Concorrencia.ThreadPool;
import kahoot.game.Team;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer extends Thread {


    private static final Map<String, GameRoom> mapaCodigoParaSala = new ConcurrentHashMap<>();

    private static final ThreadPool poolDeJogos = new ThreadPool(5);

    private boolean running = true;
    private static final int PORT = 12345;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(" SERVIDOR CENTRAL (RECEÇÃO) À ESCUTA NA PORTA " + PORT + "...");
            System.out.println(" Pronto para receber conexões de múltiplos jogos simultâneos.");

            while (running) {
                Socket socket = serverSocket.accept();


                new GameHandler(socket, this).start();
            }

        } catch (IOException e) {
            System.err.println(" Erro no Servidor Central: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void registarNovoJogo(GameRoom sala, Map<String, Team> equipas) {
        for (String codigo : equipas.keySet()) {
            mapaCodigoParaSala.put(codigo, sala);
        }
        System.out.println(" Jogo [" + sala.getId() + "] registado com sucesso.");
        System.out.println("   ➡ Códigos ativos para esta sala: " + equipas.keySet());
    }

    public static void iniciarJogo(Runnable gameLoop) {
        System.out.println(" Jogo submetido à ThreadPool Global.");
        poolDeJogos.execute(gameLoop);
    }



    public GameRoom descobrirSala(String codigoEquipa) {
        return mapaCodigoParaSala.get(codigoEquipa);
    }


    public void stopServer() {
        this.running = false;
    }
}