package kahoot.game;

import com.google.gson.Gson;
import java.io.FileReader;
import java.util.List;

public class QuizLoader {

    public static List<Question> load(String filePath) {

        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Quiz quiz = gson.fromJson(reader, Quiz.class);

            return quiz.questions;
        } catch (Exception e) {
            System.err.println("Erro ao ler JSON: " + e.getMessage());
            return List.of();
        }
    }
}
