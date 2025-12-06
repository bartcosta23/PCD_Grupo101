package kahoot.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
// IMPORTANTE: Não importar java.util.concurrent.ConcurrentHashMap
// O Java vai usar a tua classe automaticamente se estiver no mesmo package
// ou deves importar a tua se estiver noutro package.

public class GameState {

    private final List<Question> perguntas;
    private int indiceAtual;

    // Usando a TUA classe ConcurrentHashMap
    private final ConcurrentHashMap<String, Integer> pontuacoes;

    public GameState(List<Question> perguntas) {
        // 1. Criar cópia e baralhar para garantir a aleatoriedade
        this.perguntas = new ArrayList<>(perguntas);
        Collections.shuffle(this.perguntas);

        this.indiceAtual = 0;

        // 2. Inicializar a tua estrutura de dados
        this.pontuacoes = new ConcurrentHashMap<>();
    }

    // --- LÓGICA DE JOGO (INDIVIDUAL VS EQUIPA) ---

    /**
     * Define se a ronda atual é de Equipa ou Individual.
     * O enunciado diz que alternam. Vamos assumir:
     * Ronda 0 (Par) -> Individual
     * Ronda 1 (Ímpar) -> Equipa
     * Ronda 2 (Par) -> Individual...
     */
    public synchronized boolean isRoundTeam() {
        // Se o índice for ímpar, é equipa. Se for par, é individual.
        return (indiceAtual % 2) != 0;
    }

    // --- MÉTODOS DE CONTROLO DE RONDAS ---

    public synchronized boolean temPerguntaAtual() {
        return indiceAtual < perguntas.size();
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
        return indiceAtual >= perguntas.size();
    }

    // --- GESTÃO DE PONTUAÇÃO (Usando o teu Map) ---

    public void adicionarPontos(String jogador, int pontos) {
        // A tua classe ConcurrentHashMap tem o método merge implementado
        // Isto é thread-safe porque o método merge da tua classe é synchronized
        pontuacoes.merge(jogador, pontos, Integer::sum);
    }

    public int getPontuacao(String jogador) {
        // A tua classe tem getOrDefault e é synchronized
        return pontuacoes.getOrDefault(jogador, 0);
    }

    public synchronized Map<String, Integer> getPlacar() {
        return pontuacoes.snapshot();
    }

    //public ConcurrentHashMap<String, Integer> getPlacar() {
    //    return pontuacoes;
    //}
}