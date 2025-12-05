package kahoot.gui;

import kahoot.game.Question;
import kahoot.game.Team;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.List;

public class MainGuiDemo {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Socket socket;

    private static Gui gui;
    private static JButton[] botoes;

    // TIMER
    private static Timer timer;
    private static int segundosRestantes;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GameMode modo = ModoEscolha.escolherModo();
            if (modo == null) System.exit(0);

            String username;
            String teamName = null;

            if (modo == GameMode.INDIVIDUAL) {
                username = JOptionPane.showInputDialog(null, "Qual é o teu nome?", "Login", JOptionPane.QUESTION_MESSAGE);
                if (username == null || username.isBlank()) System.exit(0);
            } else {
                Team[] equipasDisponiveis = { new Team("Azuis"), new Team("Vermelhos"), new Team("Verdes") };
                Team equipaEscolhida = TeamEscolha.selecionarEquipa(equipasDisponiveis);
                if (equipaEscolhida == null) System.exit(0);

                username = JOptionPane.showInputDialog(null, "O teu nome na equipa:", "Login", JOptionPane.QUESTION_MESSAGE);
                teamName = equipaEscolhida.getNome();
            }

            if (!conectarAoServidor(username, teamName)) {
                return;
            }

            gui = new Gui();
            gui.setVisible(true);
            gui.log("🔌 Ligado ao servidor. À espera de perguntas...");

            botoes = new JButton[]{
                    gui.getBotaoOpcaoA(), gui.getBotaoOpcaoB(),
                    gui.getBotaoOpcaoC(), gui.getBotaoOpcaoD()
            };

            configurarBotoes();

            new Thread(MainGuiDemo::ouvirServidor).start();
        });
    }

    private static boolean conectarAoServidor(String username, String teamName) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            String[] dadosLogin = {username, teamName};
            out.writeObject(new Mensagem(MessagesEnum.LOGIN, dadosLogin));
            out.flush();

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao ligar ao servidor: " + e.getMessage());
            return false;
        }
    }

    private static void configurarBotoes() {
        for (int i = 0; i < botoes.length; i++) {
            int indiceOpcao = i;
            botoes[i].addActionListener(e -> {
                enviarResposta(indiceOpcao);
            });
        }
    }

    private static void enviarResposta(int indiceOpcao) {
        try {
            for (JButton b : botoes) b.setEnabled(false);

            out.writeObject(new Mensagem(MessagesEnum.ANSWER, indiceOpcao));
            out.flush();

            gui.log("📤 Resposta enviada: " + (indiceOpcao + 1));

            pararTimer();

        } catch (IOException ex) {
            gui.log("❌ Erro ao enviar resposta.");
        }
    }

    private static void ouvirServidor() {
        try {
            while (true) {
                Mensagem msg = (Mensagem) in.readObject();
                SwingUtilities.invokeLater(() -> processarMensagem(msg));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> gui.log("❌ Desconectado do servidor."));
        }
    }

    private static void processarMensagem(Mensagem msg) {
        switch (msg.getType()) {

            case QUESTION:
                Question q = (Question) msg.getContent();
                mostrarNovaPergunta(q);
                break;

            case ANSWER_RESULT:
                Object[] data = (Object[]) msg.getContent();
                mostrarFeedbackResposta((int) data[0], (boolean) data[1]);
                break;

            case SCORE:
                @SuppressWarnings("unchecked")
                Map<String, Integer> placar = (Map<String, Integer>) msg.getContent();
                gui.atualizarClassificacao(placar);
                break;

            default:
                gui.log("Mensagem desconhecida recebida.");
        }
    }

    private static void mostrarNovaPergunta(Question q) {

        // Reset de cores SEMPRE ao receber nova pergunta
        for (JButton b : botoes) {
            b.setBackground(null);
            b.setEnabled(true);
        }

        gui.atualizarPergunta(q.getText());

        List<String> opcoes = q.getOptions();
        for (int i = 0; i < botoes.length; i++) {
            if (i < opcoes.size()) {
                botoes[i].setText(opcoes.get(i));
                botoes[i].setEnabled(true);
            } else {
                botoes[i].setText("");
                botoes[i].setEnabled(false);
            }
        }

        gui.log("❓ Nova pergunta recebida!");
        iniciarTimer();
    }

    // ---------------------------
    // FEEDBACK VISUAL
    // ---------------------------

    private static void mostrarFeedbackResposta(int index, boolean acertou) {
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].setEnabled(false);
        }

        if (acertou) botoes[index].setBackground(Color.GREEN);
        else botoes[index].setBackground(Color.RED);

        gui.log("🎯 Resultado recebido!");
    }



    // ---------------------------
    // TIMER DE 10 SEGUNDOS
    // ---------------------------

    private static void iniciarTimer() {
        pararTimer(); // caso estivesse ativo

        segundosRestantes = 10;
        gui.atualizarTimer(segundosRestantes);

        timer = new Timer(1000, e -> {
            segundosRestantes--;
            gui.atualizarTimer(segundosRestantes);

            if (segundosRestantes <= 0) {
                pararTimer();
                tempoEsgotado();
            }
        });

        timer.start();
    }

    private static void pararTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private static void tempoEsgotado() {

        gui.log("⏳ Tempo esgotado!");

        try {
            out.writeObject(new Mensagem(MessagesEnum.ANSWER, -1));
            out.flush();
        } catch (IOException ex) {
            gui.log("❌ Erro ao enviar timeout.");
        }

        for (JButton b : botoes) b.setEnabled(false);
    }
}
