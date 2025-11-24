package kahoot.messages;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private String autor;
    private String texto;

    public Mensagem(String autor, String texto) {
        this.autor = autor;
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }
}
