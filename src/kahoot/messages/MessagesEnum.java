package kahoot.messages;

public enum MessagesEnum {
    LOGIN,      // Cliente -> Servidor (Conteúdo: String[] {user, team})
    QUESTION,   // Servidor -> Cliente (Conteúdo: objeto Question)
    ANSWER,     // Cliente -> Servidor (Conteúdo: Integer index)
    SCORE,      // Servidor -> Cliente (Conteúdo: Map placar)
    ERROR       // Servidor -> Cliente (Conteúdo: String erro)
}