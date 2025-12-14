package kahoot.game;

import kahoot.Concorrencia.TeamBarrier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {

    private final String nome;
    private final List<Player> membros;


    private transient TeamBarrier barreiraAtual;

    public Team(String nome) {
        this.nome = nome;
        this.membros = new ArrayList<>();
    }


    public synchronized boolean addPlayer(Player p) {
        if (membros.size() >= 2) {
            return false; // Equipa cheia
        }
        membros.add(p);
        p.setTeam(this);
        return true;
    }

    public synchronized List<Player> getMembers() {
        return new ArrayList<>(membros);
    }

    public synchronized boolean isFull() {
        return membros.size() == 2;
    }


    public void setBarreiraAtual(TeamBarrier barreira) {
        this.barreiraAtual = barreira;
    }

    public TeamBarrier getBarreiraAtual() {
        return barreiraAtual;
    }


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