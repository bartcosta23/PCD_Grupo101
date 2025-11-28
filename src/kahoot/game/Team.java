package kahoot.game;

import kahoot.Concorrencia.TeamBarrier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {

    private final String nome;
    private final List<Player> membros;

    // Campo Transiente (não é enviado pela rede, fica só no servidor)
    // Serve para os jogadores da equipa se sincronizarem na ronda atual
    private transient TeamBarrier barreiraAtual;

    public Team(String nome) {
        this.nome = nome;
        this.membros = new ArrayList<>();
    }

    // --- GESTÃO DE MEMBROS ---

    public synchronized boolean addPlayer(Player p) {
        // O enunciado diz equipas de 2 (podes ajustar este limite)
        if (membros.size() >= 2) {
            return false; // Equipa cheia
        }
        membros.add(p);
        p.setTeam(this); // Liga o jogador a esta equipa
        return true;
    }

    public synchronized List<Player> getMembers() {
        return new ArrayList<>(membros); // Retorna cópia para segurança
    }

    public synchronized boolean isFull() {
        return membros.size() == 2;
    }

    // --- CONCORRÊNCIA (A parte importante para a avaliação) ---

    public void setBarreiraAtual(TeamBarrier barreira) {
        this.barreiraAtual = barreira;
    }

    public TeamBarrier getBarreiraAtual() {
        return barreiraAtual;
    }

    // --- PONTUAÇÃO E DADOS ---

    public String getNome() {
        return nome;
    }

    public synchronized int getPontuacaoTotal() {
        int total = 0;
        for (Player p : membros) {
            total += p.getScore();
        }
        return total;
    }

    @Override
    public String toString() {
        return nome + " (" + membros.size() + "/2 jogadores)";
    }

    // Necessário para comparações corretas em listas/mapas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return nome.equals(team.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}