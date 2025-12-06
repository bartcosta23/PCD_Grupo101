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
import java.util.List;
import java.util.Map;

public class MainGuiDemo {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Socket socket;
    private static Gui gui;
    private static JButton[] botoes;

    // Timer
    private static GameTimer timer;
    private static int segundosRestantes;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Pedir Login
            String username = JOptionPane.showInputDialog(null, "👤 Nome de Jogador:", "Login", JOptionPane.QUESTION_MESSAGE);
            if (username == null || username.isBlank()) System.exit(0);

            String codigoEquipa = JOptionPane.showInputDialog(null, "🔑 Código da Equipa:", "Login", JOptionPane.QUESTION_MESSAGE);
            if (codigoEquipa == null || codigoEquipa.isBlank()) System.exit(0);

            // 2. Conectar
            if (!conectarAoServidor(username, codigoEquipa.trim().toUpperCase())) {
                return;
            }

            // 3. Iniciar GUI
            gui = new Gui();
            gui.setVisible(true);
            gui.log("🔌 Ligado! Equipa: " + codigoEquipa);

            // Referências para os botões da GUI
            botoes = new JButton[]{
                    gui.getBotaoOpcaoA(), gui.getBotaoOpcaoB(),
                    gui.getBotaoOpcaoC(), gui.getBotaoOpcaoD()
            };
            configurarBotoes();

            // 4. Thread para ouvir mensagens do servidor
            new Thread(MainGuiDemo::ouvirServidor).start();
        });
    }

    private static boolean conectarAoServidor(String username, String codigoEquipa) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Envia Login
            String[] dados = {username, codigoEquipa};
            out.writeObject(new Mensagem(MessagesEnum.LOGIN, dados));
            out.flush();

            // Espera resposta "OK"
            Mensagem resposta = (Mensagem) in.readObject();
            if (resposta.getType() == MessagesEnum.LOGIN && "OK".equals(resposta.getContent())) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "❌ Erro no Login: " + resposta.getContent());
                socket.close();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro de conexão: " + e.getMessage());
            return false;
        }
    }

    private static void configurarBotoes() {
        for (int i = 0; i < botoes.length; i++) {
            int index = i;
            botoes[i].addActionListener(e -> enviarResposta(index));
        }
    }

    private static void enviarResposta(int index) {
        try {
            // Bloqueia botões para não clicar duas vezes
            for (JButton b : botoes) b.setEnabled(false);

            out.writeObject(new Mensagem(MessagesEnum.ANSWER, index));
            out.flush();

            gui.log("📤 Resposta enviada. A aguardar...");
            pararTimer();

        } catch (IOException ex) {
            gui.log("❌ Erro ao enviar.");
        }
    }

    private static void ouvirServidor() {
        try {
            while (true) {
                Mensagem msg = (Mensagem) in.readObject();
                // Processar na Thread da GUI para evitar erros visuais
                SwingUtilities.invokeLater(() -> processarMensagem(msg));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> gui.log("❌ Desconectado."));
        }
    }

    // ================================================================
    // 🔥 LÓGICA PRINCIPAL: Recebe mensagens e atualiza a GUI
    // ================================================================
    private static void processarMensagem(Mensagem msg) {
        switch (msg.getType()) {
            case QUESTION -> {
                // O Servidor agora envia um Array: [Pergunta, Booleano]
                Object[] dados = (Object[]) msg.getContent();
                Question q = (Question) dados[0];
                boolean isTeamMode = (boolean) dados[1];

                mostrarNovaPergunta(q, isTeamMode);
            }
            case ANSWER_RESULT -> {
                // Recebe: [Indice, Acertou?]
                Object[] dados = (Object[]) msg.getContent();
                int index = (int) dados[0];
                boolean acertou = (boolean) dados[1];

                mostrarFeedback(index, acertou);
            }
            case SCORE -> {
                // Recebe o Mapa de Pontos
                Map<String, Integer> placar = (Map<String, Integer>) msg.getContent();
                gui.atualizarClassificacao(placar);
            }
        }
    }

    private static void mostrarNovaPergunta(Question q, boolean isTeamMode) {
        // 1. Resetar Botões (Tirar cores antigas)
        for (JButton b : botoes) {
            b.setBackground(null);
            b.setEnabled(true);
            b.setOpaque(true); // Garante que a cor reseta
        }

        // 2. 🔥 ATUALIZAR O TÍTULO DO MODO (A TUA ALTERAÇÃO)
        if (isTeamMode) {
            gui.atualizarModo("MODO EQUIPA", Color.black);
        } else {
            gui.atualizarModo("MODO INDIVIDUAL", Color.black);
        }

        // 3. Atualizar Texto da Pergunta
        gui.atualizarPergunta(q.getText());

        List<String> opcoes = q.getOptions();
        gui.atualizarOpcoes(opcoes.toArray(new String[0]));

        iniciarTimer();
    }

    private static void mostrarFeedback(int index, boolean acertou) {
        // Garante que botões estão bloqueados
        for (JButton b : botoes) b.setEnabled(false);

        if (index >= 0 && index < 4) {
            JButton btn = botoes[index];
            btn.setOpaque(true); // Necessário em alguns sistemas (Mac/Linux)
            btn.setBorderPainted(false); // Ajuda a cor a aparecer

            if (acertou) {
                btn.setBackground(Color.GREEN);
                gui.log("✅ Acertaste!");
            } else {
                btn.setBackground(Color.RED);
                gui.log("❌ Erraste!");
            }
        } else {
            gui.log("⚠️ Tempo esgotado.");
        }
    }

    // ================= TIMER =================
    private static void iniciarTimer() {
        pararTimer();
        segundosRestantes = 15; // 15 segundos por pergunta
        gui.atualizarTimer(segundosRestantes);

        timer = new GameTimer(15,
                () -> { // Tick
                    segundosRestantes--;
                    gui.atualizarTimer(segundosRestantes);
                },
                () -> { // Finish
                    tempoEsgotado();
                }
        );
        timer.start();
    }

    private static void pararTimer() {
        if (timer != null) {
            timer.parar();
            timer = null;
        }
    }

    private static void tempoEsgotado() {
        gui.log("⏳ Tempo acabou!");
        for (JButton b : botoes) b.setEnabled(false);
        try {
            out.writeObject(new Mensagem(MessagesEnum.ANSWER, -1));
            out.flush();
        } catch (IOException e) {}
    }
}