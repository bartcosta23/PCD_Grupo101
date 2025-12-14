package kahoot.server;

import kahoot.game.*;
import kahoot.messages.Mensagem;
import kahoot.Concorrencia.TeamBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameRoom {

    private final String idJogo;
    private final Map<String, Team> equipasDoJogo;
    private final List<GameHandler> jogadoresConectados = new ArrayList<>();
    private GameState gameState;

    private TeamBarrier lobbyBarrier;
    private final ExecutorService roomThreadPool = Executors.newCachedThreadPool();
    private boolean jogoIniciado = false;

    public GameRoom(String idJogo, Map<String, Team> equipas, List<Question> perguntas) {
        this.idJogo = idJogo;
        this.equipasDoJogo = equipas;
        this.gameState = new GameState(perguntas);


        // Se tens 2 equipas, queres 4 jogadores (2 por equipa).
        // Antes estava apenas "equipas.size()", o que deixava o jogo começar meio vazio.
        int totalJogadores = equipas.size() * 2;

        this.lobbyBarrier = new TeamBarrier(totalJogadores, 300000, () -> {
            System.out.println(" [" + idJogo + "] Sala cheia! A iniciar GameLoop...");
            this.jogoIniciado = true;

            // Passamos 'this' (A Sala) em vez do Server
            GameServer.iniciarJogo(new GameLoop(this, gameState));
        });
    }

    // ... (Resto do código igual: adicionarJogador, getters, broadcast, etc.) ...

    public void adicionarJogador(GameHandler handler) {
        jogadoresConectados.add(handler);
        roomThreadPool.execute(handler);
    }

    public synchronized void removerJogador(GameHandler handler) {
        jogadoresConectados.remove(handler);
    }

    public Team getTeamByCode(String code) { return equipasDoJogo.get(code); }
    public TeamBarrier getLobbyBarrier() { return lobbyBarrier; }
    public List<GameHandler> getClients() { return jogadoresConectados; }
    public String getId() { return idJogo; }
    public GameState getGameState() { return gameState; }

    public synchronized void broadcast(Mensagem msg) {
        for (GameHandler h : jogadoresConectados) h.send(msg);
    }
}