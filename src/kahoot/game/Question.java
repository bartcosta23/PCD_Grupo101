package kahoot.game;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {

    // Nomes dos campos devem bater certo com o JSON para o GSON funcionar automaticamente
    private String question;
    private List<String> options;
    private int correct; // Índice da resposta correta (ex: 0, 1, 2 ou 3)
    private int points;

    public Question(String question, List<String> options, int correct, int points) {
        this.question = question;
        this.options = options;
        this.correct = correct;
        this.points = points;
    }

    //  O MÉTODO QUE FALTA
    public boolean isCorrect(int indexEscolhido) {
        // Compara o índice que o jogador enviou com o índice correto guardado
        return this.correct == indexEscolhido;
    }

    //  GETTERS
    public String getText() { return question; }
    public List<String> getOptions() { return options; }
    public int getPoints() { return points; }

    @Override
    public String toString() {
        return question;
    }
}