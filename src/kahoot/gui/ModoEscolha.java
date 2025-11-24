package kahoot.gui;

import javax.swing.*;

public class ModoEscolha {

    public static GameMode escolherModo() {
        Object[] opcoes = { "Individual", "Equipa" };

        int escolha = JOptionPane.showOptionDialog(
                null,
                "Como queres jogar?",
                "Modo de Jogo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (escolha == 0) return GameMode.INDIVIDUAL;
        if (escolha == 1) return GameMode.EQUIPA;

        return null; // cancelou
    }
}
