package kahoot.game;

import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {

    private final String username;
    private int score;
    private Team team; // Referência para a equipa (pode ser null se for jogo individual)

    public Player(String username) {
        this.username = username;
        this.score = 0;
        this.team = null;
    }

    // --- MÉTODOS DE DADOS (Imutáveis ou Simples) ---

    public String getUsername() {
        return username;
    }

    // --- MÉTODOS DE EQUIPA ---

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public boolean hasTeam() {
        return team != null;
    }

    // --- MÉTODOS DE PONTUAÇÃO (Sincronizados) ---
    // Estes métodos precisam de ser synchronized porque:
    // 1. O GameHandler escreve (quando o jogador acerta).
    // 2. O GameState lê (quando envia o placar no fim da ronda).

    public synchronized void addScore(int points) {
        this.score += points;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized void resetScore() {
        this.score = 0;
    }

    // --- IDENTIDADE (Importante para Listas e Maps) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(username, player.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + " (" + score + " pts)";
    }
}