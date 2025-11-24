package kahoot.game;

public class Team {

    private String nome;   // <- nome da equipa

    public Team(String nome) {
        this.nome = nome;
    }

    public String getNome() {   // <- ESTE MÉTODO FALTAVA
        return nome;
    }

    @Override
    public String toString() {
        return nome;   // importante para aparecer bem no JOptionPane
    }
}
