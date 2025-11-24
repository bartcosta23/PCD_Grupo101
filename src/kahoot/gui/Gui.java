package kahoot.gui;


import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Gui extends JFrame {


    private final JLabel labelPontuacao;
    private final JLabel labelTimer; // Para o cronómetro
    private final JTextArea areaPergunta;
    private final JButton botaoOpcaoA;
    private final JButton botaoOpcaoB;
    private final JButton botaoOpcaoC;
    private final JButton botaoOpcaoD;
    private final JTextArea areaLog;
    private final JTextArea areaClassificacao; // Para o leaderboard

    public Gui() {
        super("Kahoot Distribuído");

        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margem

        JPanel painelTopo = new JPanel(new BorderLayout(10, 0));
        labelPontuacao = new JLabel("Pontuação: 0");
        labelPontuacao.setFont(new Font("Arial", Font.BOLD, 18));

        labelTimer = new JLabel("Tempo: --"); // Inicialização
        labelTimer.setFont(new Font("Arial", Font.BOLD, 18));
        labelTimer.setHorizontalAlignment(SwingConstants.RIGHT);

        painelTopo.add(labelPontuacao, BorderLayout.WEST);
        painelTopo.add(labelTimer, BorderLayout.EAST);
        add(painelTopo, BorderLayout.NORTH);

        areaPergunta = new JTextArea("A pergunta aparecerá aqui...");
        areaPergunta.setFont(new Font("Arial", Font.PLAIN, 16));
        areaPergunta.setEditable(false);
        areaPergunta.setLineWrap(true);
        areaPergunta.setWrapStyleWord(true);
        add(new JScrollPane(areaPergunta), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 10, 10));
        botaoOpcaoA = new JButton("Opção A");
        botaoOpcaoB = new JButton("Opção B");
        botaoOpcaoC = new JButton("Opção C");
        botaoOpcaoD = new JButton("Opção D");

        painelBotoes.add(botaoOpcaoA);
        painelBotoes.add(botaoOpcaoB);
        painelBotoes.add(botaoOpcaoC);
        painelBotoes.add(botaoOpcaoD);
        add(painelBotoes, BorderLayout.SOUTH);


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

        setSize(800, 500);
        setMinimumSize(new Dimension(700, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }


    public void atualizarPergunta(String texto) {
        SwingUtilities.invokeLater(() -> areaPergunta.setText(texto));
    }

    public void atualizarPontuacao(int pontos) {
        SwingUtilities.invokeLater(() -> labelPontuacao.setText("Pontuação: " + pontos));
    }

    public void atualizarTimer(int tempo) {
        SwingUtilities.invokeLater(() -> labelTimer.setText("Tempo: " + tempo));
    }


    public void atualizarClassificacao(Map<String, Integer> placar) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Classificação:\n");
            placar.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        sb.append(String.format("- %s: %d\n", entry.getKey(), entry.getValue()));
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
        SwingUtilities.invokeLater(() -> areaLog.append(mensagem + "\n"));
    }

    public JButton getBotaoOpcaoA() { return botaoOpcaoA; }
    public JButton getBotaoOpcaoB() { return botaoOpcaoB; }
    public JButton getBotaoOpcaoC() { return botaoOpcaoC; }
    public JButton getBotaoOpcaoD() { return botaoOpcaoD; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Gui().setVisible(true));
    }
}    
