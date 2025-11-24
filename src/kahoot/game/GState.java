package kahoot.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GState {

    private final List<Question> perguntas;
    private int indiceAtual;

    // Thread-safe
    private final Map<String, Integer> pontuacoes = new ConcurrentHashMap<>();

    public GState(List<Question> perguntas) {
        this.perguntas = perguntas;
        this.indiceAtual = 0;
    }

    
    //  MÉTODOS PRINCIPAIS
  
    // Retorna a pergunta atual 
    public synchronized Question getPerguntaAtual() {
        if (indiceAtual < perguntas.size()) {
            return perguntas.get(indiceAtual);
        }
        return null;
    }

    // Avança para a próxima pergunta
    public synchronized boolean proximaPergunta() {
        if (indiceAtual + 1 < perguntas.size()) {
            indiceAtual++;
            return true;
        }
        return false;
    }

    // Atualiza pontuação da equipa/jogador 
    public synchronized void adicionarPontos(String jogador, int pontos) {
        pontuacoes.merge(jogador, pontos, Integer::sum);
    }

    // Devolve a pontuação total 
    public int getPontuacao(String jogador) {
        return pontuacoes.getOrDefault(jogador, 0);
    }

    // Verifica se acabou 
    public synchronized boolean terminou() {
        return indiceAtual >= perguntas.size() - 1;
    }

    // Devolve uma cópia segura do placar 
    public synchronized Map<String, Integer> getPlacar() {
        return new HashMap<>(pontuacoes);
    }
}
