package kahoot.game;

import java.util.*;

public class GameState {

    private List<Question> perguntas;
    private int indiceAtual;
    private Map<String, Integer> pontuacoes; 

    public GameState(List<Question> perguntas) {
        this.perguntas = perguntas;
        this.indiceAtual = 0;
        this.pontuacoes = new HashMap<>();
    }

    // metodos principais

    //retorna a pergunta
    public Question getPerguntaAtual() {
        if (indiceAtual < perguntas.size()) {
            return perguntas.get(indiceAtual);
        }
        return null;
    }

    // avança para a prox pergunta
    public boolean proximaPergunta() {
        if (indiceAtual + 1 < perguntas.size()) {
            indiceAtual++;
            return true;
        }
        return false;
    }

    //atualiza a pontuação do jogador
    public void adicionarPontos(String jogador, int pontos) {
        pontuacoes.put(jogador, pontuacoes.getOrDefault(jogador, 0) + pontos);
    }

    //retorna  a pontuacao total
    public int getPontuacao(String jogador) {
        return pontuacoes.getOrDefault(jogador, 0);
    }

    // verifica se o jogo terminou
    public boolean terminou() {
        return indiceAtual >= perguntas.size() - 1;
    }

    // retorna o placar completo
    public Map<String, Integer> getPlacar() {
        return Collections.unmodifiableMap(pontuacoes);
    }
}
