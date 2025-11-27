package kahoot.gui;

import com.google.gson.Gson;
import kahoot.game.GState;
import kahoot.game.Question;
import kahoot.game.Quiz;
import kahoot.game.Team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainGuiDemo {

    private static final int TEMPO_POR_PERGUNTA = 10; // segundos
    private static Timer swingTimer;
    private static AtomicInteger tempoRestante;

    public static void main(String[] args) {

    	SwingUtilities.invokeLater(() -> {

    	    // =============================
    	    // 1) Escolher modo de jogo
    	    // =============================
    	    GameMode modo = ModoEscolha.escolherModo();

    	    if (modo == null) {
    	        JOptionPane.showMessageDialog(null, "Nenhum modo selecionado!", "Erro", JOptionPane.ERROR_MESSAGE);
    	        System.exit(0);
    	    }

    	    // Nome/jogador ou equipa
    	    String jogador;

    	    if (modo == GameMode.INDIVIDUAL) {
    	        jogador = JOptionPane.showInputDialog(
    	                null,
    	                "Qual é o teu nome?",
    	                "Jogador Individual",
    	                JOptionPane.QUESTION_MESSAGE
    	        );

    	        if (jogador == null || jogador.isBlank()) {
    	            JOptionPane.showMessageDialog(null, "Nome inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
    	            System.exit(0);
    	        }

    	    } else { // EQUIPA

    	        Team[] equipasDisponiveis = {
    	                new Team("Azuis"),
    	                new Team("Vermelhos"),
    	                new Team("Verdes")
    	        };

    	        Team equipaEscolhida = TeamEscolha.selecionarEquipa(equipasDisponiveis);

    	        if (equipaEscolhida == null) {
    	            JOptionPane.showMessageDialog(null, "Nenhuma equipa selecionada!", "Erro", JOptionPane.ERROR_MESSAGE);
    	            System.exit(0);
    	        }

    	        jogador = equipaEscolhida.getNome(); // usa o nome da equipa
    	    }

    	    System.out.println("Modo = " + modo);
    	    System.out.println("Jogador/Equipa = " + jogador);
            // ==================================
            // 2) Criar GUI
            // ==================================
            Gui gui = new Gui();
            gui.setVisible(true);

            // ==================================
            // 3) Carregar perguntas do JSON
            // ==================================
            List<Question> perguntas;
            try (FileReader reader = new FileReader("quizzes.json")) {
                Gson gson = new Gson();
                Quiz quiz = gson.fromJson(reader, Quiz.class);
                perguntas = quiz.questions;
            } catch (Exception e) {
                gui.log("❌ Erro ao ler quizzes.json: " + e.getMessage());
                return;
            }

            if (perguntas == null || perguntas.isEmpty()) {
                gui.log("❌ Nenhuma pergunta encontrada!");
                return;
            }

            // Estado do jogo
            GState estado = new GState(perguntas);

            

            // Botões
            JButton[] botoes = {
                    gui.getBotaoOpcaoA(),
                    gui.getBotaoOpcaoB(),
                    gui.getBotaoOpcaoC(),
                    gui.getBotaoOpcaoD()
            };


            final Runnable[] mostrarPerguntaHolder = new Runnable[1];
            final Runnable[] irParaProximaHolder = new Runnable[1];

            tempoRestante = new AtomicInteger(TEMPO_POR_PERGUNTA);

            // ==================================
            // 4) Função quando o tempo acaba
            // ==================================
            Runnable esgotouTempo = () -> {
                swingTimer.stop();
                Question q = estado.getPerguntaAtual();
                if (q == null) return;

                for (JButton b : botoes) b.setEnabled(false);
                botoes[q.correct].setBackground(new Color(144, 238, 144));

                gui.log("⏰ Tempo esgotado! A correta era: " + q.options[q.correct]);

                new Timer(1500, ev -> {
                    ((Timer) ev.getSource()).stop();
                    irParaProximaHolder[0].run();
                }).start();
            };

            // Timer
            swingTimer = new Timer(1000, e -> {
                int tempo = tempoRestante.decrementAndGet();
                gui.atualizarTimer(tempo);
                if (tempo <= 0) esgotouTempo.run();
            });

            // ==================================
            // 5) Avançar para próxima pergunta
            // ==================================
            irParaProximaHolder[0] = () -> {
                if (estado.proximaPergunta()) {
                    mostrarPerguntaHolder[0].run();
                } else {
                    swingTimer.stop();
                    gui.atualizarPergunta("🏁 Fim do quiz! Pontuação final: " +
                            estado.getPontuacao(jogador));
                    gui.atualizarOpcoes(new String[]{});
                    gui.log("🎉 Jogo terminado!");
                    gui.atualizarClassificacao(estado.getPlacar());
                }
            };

            // ==================================
            // 6) Mostrar pergunta
            // ==================================
            mostrarPerguntaHolder[0] = () -> {
                Question q = estado.getPerguntaAtual();
                if (q == null) return;

                gui.atualizarPergunta(q.question);

                for (int i = 0; i < botoes.length; i++) {
                    String prefixo = switch (i) {
                        case 0 -> "A) ";
                        case 1 -> "B) ";
                        case 2 -> "C) ";
                        case 3 -> "D) ";
                        default -> "";
                    };

                    botoes[i].setText(prefixo + q.options[i]);
                    botoes[i].setEnabled(true);
                    botoes[i].setBackground(null);
                }

                gui.atualizarPontuacao(estado.getPontuacao(jogador));
                gui.atualizarClassificacao(estado.getPlacar());

                tempoRestante.set(TEMPO_POR_PERGUNTA);
                gui.atualizarTimer(TEMPO_POR_PERGUNTA);
                swingTimer.start();
            };

            // ==================================
            // 7) Listener das respostas
            // ==================================
            ActionListener responder = e -> {
                swingTimer.stop();

                Question q = estado.getPerguntaAtual();
                JButton escolhido = (JButton) e.getSource();

                int resposta = -1;
                for (int i = 0; i < botoes.length; i++) {
                    if (botoes[i] == escolhido) resposta = i;
                }

                for (JButton b : botoes) b.setEnabled(false);

                if (resposta == q.correct) {
                    escolhido.setBackground(new Color(144, 238, 144)); // Verde
                    gui.log("✅ Correto! +" + q.points + " pontos");
                    estado.adicionarPontos(jogador, q.points);
                    gui.atualizarPontuacao(estado.getPontuacao(jogador));
                } else {
                    escolhido.setBackground(new Color(255, 99, 71)); // Vermelho
                    botoes[q.correct].setBackground(new Color(144, 238, 144));
                    gui.log("❌ Errado. Correta: " + q.options[q.correct]);
                }

                gui.atualizarClassificacao(estado.getPlacar());

                new Timer(1500, ev -> {
                    ((Timer) ev.getSource()).stop();
                    irParaProximaHolder[0].run();
                }).start();
            };

            for (JButton botao : botoes) botao.addActionListener(responder);

            // Primeira pergunta
            mostrarPerguntaHolder[0].run();
        });
    }
}

// teste push