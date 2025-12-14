package kahoot.game;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {

    private String question;
    private List<String> options;
    private int correct;
    private int points;

    public Question(String question, List<String> options, int correct, int points) {
        this.question = question;
        this.options = options;
        this.correct = correct;
        this.points = points;
    }

    public boolean isCorrect(int indexEscolhido) {
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