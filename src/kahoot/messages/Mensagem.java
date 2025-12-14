package kahoot.messages;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessagesEnum type;
    private final Object content;

    // Construtor
    public Mensagem(MessagesEnum type, Object content) {
        this.type = type;
        this.content = content;
    }



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