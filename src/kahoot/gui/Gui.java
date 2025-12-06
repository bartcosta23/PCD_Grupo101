package kahoot.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Gui extends JFrame {

    private final JLabel labelPontuacao;
    private final JLabel labelModo; // 🔥 NOVO: Onde vai aparecer "EQUIPA" ou "INDIVIDUAL"
    private final JLabel labelTimer;
    private final JTextArea areaPergunta;
    private final JButton botaoOpcaoA;
    private final JButton botaoOpcaoB;
    private final JButton botaoOpcaoC;
    private final JButton botaoOpcaoD;
    private final JTextArea areaLog;
    private final JTextArea areaClassificacao;

    public Gui() {
        super("Kahoot Distribuído");

        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margem

        // --- PAINEL DE TOPO (Pontos - MODO - Timer) ---
        JPanel painelTopo = new JPanel(new BorderLayout(10, 0));

        labelPontuacao = new JLabel("Pontuação: 0");
        labelPontuacao.setFont(new Font("Arial", Font.BOLD, 16));

        labelTimer = new JLabel("Tempo: --");
        labelTimer.setFont(new Font("Arial", Font.BOLD, 16));
        labelTimer.setHorizontalAlignment(SwingConstants.RIGHT);

        // 🔥 O Label do Centro (Modo de Jogo)
        labelModo = new JLabel("A aguardar...", SwingConstants.CENTER);
        labelModo.setFont(new Font("Arial", Font.BOLD, 18));
        labelModo.setForeground(Color.BLUE);

        painelTopo.add(labelPontuacao, BorderLayout.WEST);
        painelTopo.add(labelModo, BorderLayout.CENTER); // Adicionado ao meio
        painelTopo.add(labelTimer, BorderLayout.EAST);

        add(painelTopo, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (Pergunta) ---
        areaPergunta = new JTextArea("A pergunta aparecerá aqui...");
        areaPergunta.setFont(new Font("Arial", Font.PLAIN, 18));
        areaPergunta.setEditable(false);
        areaPergunta.setLineWrap(true);
        areaPergunta.setWrapStyleWord(true);
        // Margem interna para o texto não colar à borda
        areaPergunta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(areaPergunta), BorderLayout.CENTER);

        // --- PAINEL SUL (Botões) ---
        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 10, 10));
        botaoOpcaoA = new JButton("Opção A");
        botaoOpcaoB = new JButton("Opção B");
        botaoOpcaoC = new JButton("Opção C");
        botaoOpcaoD = new JButton("Opção D");

        // Fonte maior para os botões
        Font fontBotoes = new Font("Arial", Font.BOLD, 14);
        botaoOpcaoA.setFont(fontBotoes);
        botaoOpcaoB.setFont(fontBotoes);
        botaoOpcaoC.setFont(fontBotoes);
        botaoOpcaoD.setFont(fontBotoes);

        painelBotoes.add(botaoOpcaoA);
        painelBotoes.add(botaoOpcaoB);
        painelBotoes.add(botaoOpcaoC);
        painelBotoes.add(botaoOpcaoD);
        add(painelBotoes, BorderLayout.SOUTH);

        // --- PAINEL ESTE (Leaderboard + Logs) ---
        areaClassificacao = new JTextArea("Classificação:\n");
        areaClassificacao.setEditable(false);
        areaClassificacao.setBackground(new Color(245, 245, 245));
        areaClassificacao.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollClassificacao = new JScrollPane(areaClassificacao);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(240, 240, 240));
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaLog.setForeground(Color.GRAY);
        JScrollPane scrollLog = new JScrollPane(areaLog);

        JSplitPane splitLeste = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollClassificacao, scrollLog);
        splitLeste.setResizeWeight(0.5); // 50% para cada
        splitLeste.setPreferredSize(new Dimension(220, 0));
        splitLeste.setBorder(null);

        add(splitLeste, BorderLayout.EAST);

        // Configurações da Janela
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // ============================================
    // MÉTODOS PÚBLICOS (Usados pelo MainGuiDemo)
    // ============================================

    // 🔥 Método crucial para mudar o texto do topo
    public void atualizarModo(String texto, Color cor) {
        SwingUtilities.invokeLater(() -> {
            labelModo.setText(texto);
            labelModo.setForeground(cor);
        });
    }

    public void atualizarPergunta(String texto) {
        SwingUtilities.invokeLater(() -> areaPergunta.setText(texto));
    }

    public void atualizarPontuacao(int pontos) {
        SwingUtilities.invokeLater(() -> labelPontuacao.setText("Pontuação: " + pontos));
    }

    public void atualizarTimer(int tempo) {
        SwingUtilities.invokeLater(() -> {
            labelTimer.setText("Tempo: " + tempo);
            // Fica vermelho se faltarem menos de 5 segundos
            if (tempo <= 5) labelTimer.setForeground(Color.RED);
            else labelTimer.setForeground(Color.BLACK);
        });
    }

    public void atualizarClassificacao(Map<String, Integer> placar) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("🏆 Classificação:\n\n");
            placar.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        sb.append(String.format("%-10s : %d\n", entry.getKey(), entry.getValue()));
                    });
            areaClassificacao.setText(sb.toString());
        });
    }

    public void atualizarOpcoes(String[] opcoes) {
        SwingUtilities.invokeLater(() -> {
            botaoOpcaoA.setText(opcoes.length > 0 ? opcoes[0] : "Opção A");
            botaoOpcaoB.setText(opcoes.length > 1 ? opcoes[1] : "Opção B");
            botaoOpcaoC.setText(opcoes.length > 2 ? opcoes[2] : "Opção C");
            botaoOpcaoD.setText(opcoes.length > 3 ? opcoes[3] : "Opção D");
        });
    }

    public void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensagem + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public JButton getBotaoOpcaoA() { return botaoOpcaoA; }
    public JButton getBotaoOpcaoB() { return botaoOpcaoB; }
    public JButton getBotaoOpcaoC() { return botaoOpcaoC; }
    public JButton getBotaoOpcaoD() { return botaoOpcaoD; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Gui().setVisible(true));
    }
}