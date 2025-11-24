package kahoot.gui;

import kahoot.game.Team;
import javax.swing.*;

public class TeamEscolha {

    public static Team selecionarEquipa(Team[] equipas) {

        Team escolhida = (Team) JOptionPane.showInputDialog(
                null,
                "Escolhe a tua equipa:",
                "Seleção de Equipa",
                JOptionPane.QUESTION_MESSAGE,
                null,
                equipas,  // <-- lista de teams
                equipas[0]
        );

        return escolhida;
    }
}
