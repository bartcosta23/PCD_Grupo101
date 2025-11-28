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

    // Configuração de Rede
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5001;

    // Streams para comunicação
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Socket socket;

    // Elementos da GUI
    private static Gui gui;
    private static JButton[] botoes;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // =============================
            // 1) LOGIN (Interface Gráfica)
            // =============================
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

                // Em equipa, o username pode ser algo genérico ou pedido também
                username = JOptionPane.showInputDialog(null, "O teu nome na equipa:", "Login", JOptionPane.QUESTION_MESSAGE);
                teamName = equipaEscolhida.getNome();
            }

            // ==================================
            // 2) CONECTAR AO SERVIDOR
            // ==================================
            if (!conectarAoServidor(username, teamName)) {
                return; // Falha na conexão
            }

            // ==================================
            // 3) INICIALIZAR GUI
            // ==================================
            gui = new Gui();
            gui.setVisible(true);
            gui.log("🔌 Ligado ao servidor. À espera de perguntas...");

            // Mapear botões para fácil acesso
            botoes = new JButton[]{
                    gui.getBotaoOpcaoA(), gui.getBotaoOpcaoB(),
                    gui.getBotaoOpcaoC(), gui.getBotaoOpcaoD()
            };

            // Configurar a ação dos botões (ENVIAR RESPOSTA VIA REDE)
            configurarBotoes();

            // ==================================
            // 4) THREAD DE RECEÇÃO (Ouvir o Servidor)
            // ==================================
            new Thread(MainGuiDemo::ouvirServidor).start();
        });
    }

    private static boolean conectarAoServidor(String username, String teamName) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Enviar Mensagem de Login usando o ENUM
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
            int indiceOpcao = i; // Necessário para usar na lambda
            botoes[i].addActionListener(e -> {
                try {
                    // Bloqueia botões para não responder 2 vezes
                    for (JButton b : botoes) b.setEnabled(false);

                    // ENVIA A RESPOSTA (Apenas o índice)
                    out.writeObject(new Mensagem(MessagesEnum.ANSWER, indiceOpcao));
                    out.flush();

                    gui.log("📤 Resposta enviada: " + (indiceOpcao + 1));

                } catch (IOException ex) {
                    gui.log("❌ Erro ao enviar resposta.");
                }
            });
        }
    }

    // O Loop infinito que processa o que o Servidor manda
    private static void ouvirServidor() {
        try {
            while (true) {
                // Lê a mensagem genérica
                Mensagem msg = (Mensagem) in.readObject();

                // Atualiza a GUI (sempre dentro do invokeLater para Thread Safety)
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
                atualizarInterfaceParaPergunta(q);
                break;

            case SCORE:
                @SuppressWarnings("unchecked")
                Map<String, Integer> placar = (Map<String, Integer>) msg.getContent();
                gui.atualizarClassificacao(placar);
                gui.log("📊 Placar atualizado.");
                break;

            case ERROR:
                String erro = (String) msg.getContent();
                JOptionPane.showMessageDialog(gui, erro, "Erro do Servidor", JOptionPane.ERROR_MESSAGE);
                break;

            default:
                System.out.println("Tipo desconhecido: " + msg.getType());
        }
    }

    private static void atualizarInterfaceParaPergunta(Question q) {
        gui.atualizarPergunta(q.getText()); // Nota: verifica se na classe Question o getter é getText() ou getQuestion()

        List<String> opcoes = q.getOptions();
        for (int i = 0; i < botoes.length; i++) {
            if (i < opcoes.size()) {
                botoes[i].setText(opcoes.get(i));
                botoes[i].setEnabled(true);
                botoes[i].setBackground(null); // Reset cor
            } else {
                botoes[i].setText("");
                botoes[i].setEnabled(false);
            }
        }
        gui.log("❓ Nova pergunta recebida!");
        // O Timer agora seria gerido visualmente ou por mensagens de "Tempo Restante" do servidor
    }
}