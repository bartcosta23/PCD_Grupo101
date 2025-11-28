package kahoot.game;

import java.util.List;
import java.util.Map;

public class GameState {

    private final List<Question> perguntas;
    private int indiceAtual;

    private final ConcurrentHashMap<String, Integer> pontuacoes;

    public GameState(List<Question> perguntas) {
        this.perguntas = perguntas;
        this.indiceAtual = 0;
        // Inicializa a tua estrutura segura
        this.pontuacoes = new ConcurrentHashMap<>();
    }

    public synchronized Question getPerguntaAtual() {
        if (indiceAtual < perguntas.size()) {
            return perguntas.get(indiceAtual);
        }
        return null;
    }

    public synchronized boolean proximaPergunta() {
        if (indiceAtual + 1 < perguntas.size()) {
            indiceAtual++;
            return true;
        }
        return false;
    }

    public synchronized boolean terminou() {
        return indiceAtual >= perguntas.size() - 1;
    }

    public synchronized void adicionarPontos(String jogador, int pontos) {
        pontuacoes.merge(jogador, pontos, Integer::sum);
    }

    public synchronized int getPontuacao(String jogador) {
        return pontuacoes.getOrDefault(jogador, 0);
    }

    public synchronized Map<String, Integer> getPlacar() {
        return pontuacoes.snapshot();
    }
}