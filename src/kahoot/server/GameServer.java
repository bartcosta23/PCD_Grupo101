package kahoot.server;

import kahoot.Concorrencia.ThreadPool;
import kahoot.game.Team;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVIDOR CENTRAL (RECEÇÃO)
 * - Responsável por aceitar conexões TCP na porta 12345.
 * - Não gere o jogo (isso é feito pela GameRoom).
 * - Encaminha o jogador para a sala correta com base no código da equipa.
 */
public class GameServer extends Thread {

    // MAPA MESTRE: Associa o Código da Equipa (ex: "A1B2") à Sala do Jogo (ex: JOGO-1)
    // Usamos ConcurrentHashMap porque vários clientes e a TUI acedem a isto ao mesmo tempo.
    private static final Map<String, GameRoom> mapaCodigoParaSala = new ConcurrentHashMap<>();

    private static final ThreadPool poolDeJogos = new ThreadPool(5); //apenas p teste ; valor real: 5

    private boolean running = true;
    private static final int PORT = 12345;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🟦 SERVIDOR CENTRAL (RECEÇÃO) À ESCUTA NA PORTA " + PORT + "...");
            System.out.println("🌍 Pronto para receber conexões de múltiplos jogos simultâneos.");

            while (running) {
                // 1. Aceitar conexão TCP
                Socket socket = serverSocket.accept();

                // 2. Criar um Handler "Virgem"
                // Passamos 'this' (o servidor) para que o Handler possa chamar o método descobrirSala()
                // Nota: Não adicionamos a nenhuma lista aqui. O Handler vai registar-se na GameRoom depois.
                new GameHandler(socket, this).start();
            }

        } catch (IOException e) {
            System.err.println("❌ Erro no Servidor Central: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
    //              MÉTODOS DE GESTÃO (Chamados pela TUI)
    // ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

    /**
     * Regista um novo jogo no sistema.
     * Associa todos os códigos das equipas desse jogo à respetiva sala.
     */
    public static void registarNovoJogo(GameRoom sala, Map<String, Team> equipas) {
        for (String codigo : equipas.keySet()) {
            mapaCodigoParaSala.put(codigo, sala);
        }
        System.out.println("✅ Jogo [" + sala.getId() + "] registado com sucesso.");
        System.out.println("   ➡ Códigos ativos para esta sala: " + equipas.keySet());
    }

    public static void iniciarJogo(Runnable gameLoop) {
        System.out.println("🎱 Jogo submetido à ThreadPool Global.");
        poolDeJogos.execute(gameLoop);
    }

    // ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
    //            MÉTODOS DE LOGÍSTICA (Chamados pelo Handler)
    // ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

    /**
     * O Handler chama este método quando o cliente envia o código de equipa.
     * Retorna a Sala onde esse jogo está a decorrer.
     */
    public GameRoom descobrirSala(String codigoEquipa) {
        return mapaCodigoParaSala.get(codigoEquipa);
    }

    /**
     * Permite parar o servidor graciosamente (opcional).
     */
    public void stopServer() {
        this.running = false;
    }
}