package kahoot.gui;

import kahoot.game.Question;
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

    // Timer
    private static Timer timer;
    private static int segundosRestantes;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Pedir Nome
            String username = JOptionPane.showInputDialog(null, "👤 Qual é o teu nome?", "Login Kahoot", JOptionPane.QUESTION_MESSAGE);
            if (username == null || username.isBlank()) System.exit(0);

            // 2. Pedir Código da Equipa (Fornecido pelo ServerTUI)
            String codigoEquipa = JOptionPane.showInputDialog(null, "🔑 Introduz o CÓDIGO da tua equipa:", "Código de Equipa", JOptionPane.QUESTION_MESSAGE);
            if (codigoEquipa == null || codigoEquipa.isBlank()) System.exit(0);

            // Limpa espaços e mete maiúsculas para facilitar
            codigoEquipa = codigoEquipa.trim().toUpperCase();

            // 3. Conectar
            if (!conectarAoServidor(username, codigoEquipa)) {
                return;
            }

            // 4. Iniciar GUI
            gui = new Gui();
            gui.setVisible(true);
            gui.log("🔌 Ligado! Equipa código: " + codigoEquipa);
            gui.log("Aguardando início do jogo...");

            botoes = new JButton[]{
                    gui.getBotaoOpcaoA(), gui.getBotaoOpcaoB(),
                    gui.getBotaoOpcaoC(), gui.getBotaoOpcaoD()
            };
            configurarBotoes();

            new Thread(MainGuiDemo::ouvirServidor).start();
        });
    }

    private static boolean conectarAoServidor(String username, String codigoEquipa) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 1. Enviar pedido de Login
            String[] dadosLogin = {username, codigoEquipa};
            out.writeObject(new Mensagem(MessagesEnum.LOGIN, dadosLogin));
            out.flush();

            // 2. 🔥 LER A RESPOSTA DE VALIDAÇÃO IMEDIATAMENTE
            try {
                Mensagem resposta = (Mensagem) in.readObject();

                // Verifica se o servidor disse "OK"
                if (resposta.getType() == MessagesEnum.LOGIN && "OK".equals(resposta.getContent())) {
                    return true; // Sucesso!
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Código de equipa inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                    socket.close();
                    return false;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao ligar ao servidor: " + e.getMessage());
            return false;
        }
    }

    private static void configurarBotoes() {
        for (int i = 0; i < botoes.length; i++) {
            int indiceOpcao = i;
            botoes[i].addActionListener(e -> enviarResposta(indiceOpcao));
        }
    }

    private static void enviarResposta(int indiceOpcao) {
        try {
            for (JButton b : botoes) b.setEnabled(false);
            out.writeObject(new Mensagem(MessagesEnum.ANSWER, indiceOpcao));
            out.flush();
            gui.log("📤 Resposta enviada: " + (indiceOpcao + 1));
            pararTimer();
        } catch (IOException ex) { gui.log("❌ Erro ao enviar resposta."); }
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

    // ... Copia os métodos processarMensagem, mostrarNovaPergunta, timers, etc. do teu código original aqui ...
    private static void processarMensagem(Mensagem msg) {
        switch (msg.getType()) {
            case QUESTION -> mostrarNovaPergunta((Question) msg.getContent());
            case ANSWER_RESULT -> {
                Object[] data = (Object[]) msg.getContent();
                mostrarFeedbackResposta((int) data[0], (boolean) data[1]);
            }
            case SCORE -> {
                Map<String, Integer> placar = (Map<String, Integer>) msg.getContent();
                gui.atualizarClassificacao(placar);
            }
        }
    }

    private static void mostrarNovaPergunta(Question q) {
        for (JButton b : botoes) { b.setBackground(null); b.setEnabled(true); }
        gui.atualizarPergunta(q.getText());
        List<String> opcoes = q.getOptions();
        for (int i = 0; i < botoes.length; i++) {
            if (i < opcoes.size()) { botoes[i].setText(opcoes.get(i)); botoes[i].setEnabled(true); }
            else { botoes[i].setText(""); botoes[i].setEnabled(false); }
        }
        iniciarTimer();
    }

    private static void mostrarFeedbackResposta(int index, boolean acertou) {
        for (JButton b : botoes) b.setEnabled(false);
        if (acertou) botoes[index].setBackground(Color.GREEN);
        else botoes[index].setBackground(Color.RED);
    }

    private static void iniciarTimer() {
        pararTimer();
        segundosRestantes = 10;
        gui.atualizarTimer(segundosRestantes);
        timer = new Timer(1000, e -> {
            segundosRestantes--;
            gui.atualizarTimer(segundosRestantes);
            if (segundosRestantes <= 0) { pararTimer(); tempoEsgotado(); }
        });
        timer.start();
    }
    private static void pararTimer() { if (timer != null) { timer.stop(); timer = null; } }
    private static void tempoEsgotado() {
        gui.log("⏳ Tempo esgotado!");
        try { out.writeObject(new Mensagem(MessagesEnum.ANSWER, -1)); out.flush(); } catch (Exception ignored) {}
        for (JButton b : botoes) b.setEnabled(false);
    }
}