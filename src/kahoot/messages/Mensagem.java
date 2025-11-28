package kahoot.messages;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessagesEnum type;  // O Enum (LOGIN, ANSWER, etc.)
    private final Object content; // O conteúdo (String[], Integer, etc.)

    // Construtor
    public Mensagem(MessagesEnum type, Object content) {
        this.type = type;
        this.content = content;
    }

    // --- ESTES SÃO OS MÉTODOS QUE O JAVA NÃO ESTAVA A ENCONTRAR ---

    public MessagesEnum getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Msg{" + type + "}";
    }
}