package kahoot.server;

import kahoot.Concorrencia.CountDownLatch;
import kahoot.Concorrencia.TeamBarrier;
import kahoot.game.*;
import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

import java.util.List;

public class GameLoop extends Thread {

    private final GameServer server;
    private final GameState gameState;
    private final List<GameHandler> clientes;

    // Tempo base para responder (ex: 20 segundos)
    private static final int TIMEOUT_RONDA = 20000;

    public GameLoop(GameServer server, GameState gameState) {
        this.server = server;
        this.gameState = gameState;
        this.clientes = server.getClients();
    }

    @Override
    public void run() {
        System.out.println("🎮 GameLoop iniciado.");
        esperar(2000);

        while (true) {
            // 1 ▬▬▬ Verificar se há perguntas
            if (!gameState.temPerguntaAtual()) {
                break;
            }

            Question q = gameState.getPerguntaAtual();
            boolean isTeamRound = gameState.isRoundTeam(); // Verifica o tipo de ronda

            System.out.println("📤 Enviando pergunta (" + (isTeamRound ? "EQUIPA" : "INDIVIDUAL") + "): " + q.getText());

            //server.broadcast(new Mensagem(MessagesEnum.QUESTION, q));

            Object[] pacotePergunta = new Object[]{ q, isTeamRound };
            server.broadcast(new Mensagem(MessagesEnum.QUESTION, pacotePergunta));

            // 2 ▬▬▬ PREPARAÇÃO DA CONCORRÊNCIA
            CountDownLatch mainLatch; // O Latch que segura o SERVIDOR

            if (isTeamRound) {
                // ================= MODE EQUIPA =================
                // Configurar Barreiras para cada Equipa
                List<Team> equipas = server.getTeams(); // Assume que tens este método no Server

                for (Team equipa : equipas) {
                    // Ação que corre quando a equipa toda responder (ou timeout)
                    Runnable acaoPontuacao = () -> {
                        calcularPontuacaoEquipa(equipa, q);
                    };

                    // Cria Barreira: N jogadores da equipa, Timeout, Ação
                    TeamBarrier barreira = new TeamBarrier(equipa.getMembers().size(), TIMEOUT_RONDA, acaoPontuacao);
                    equipa.setBarreiraAtual(barreira);
                }

                // Cria um Latch simples para o GameLoop esperar (sem bónus)
                // Serve apenas para acordar o servidor quando todos responderem
                mainLatch = new CountDownLatch(1, 0, TIMEOUT_RONDA, clientes.size());

            } else {
                // ================= MODE INDIVIDUAL =================
                // Lógica original: Bónus para os primeiros 3
                int bonusCount = 2; // Tem de ser taxativo, conforme o enunciado
                int bonusFactor = 2; // "pontuação será o dobro"

                // Instanciação exata conforme a API pedida:
                // (bonusFactor, bonusCount, waitPeriod, count)
                mainLatch = new CountDownLatch(bonusFactor, bonusCount, TIMEOUT_RONDA, clientes.size());

                // Limpar barreiras antigas (boa prática)
                for(Team t : server.getTeams()) t.setBarreiraAtual(null);
            }

            // 3 ▬▬▬ DISTRIBUIR O LATCH E ESPERAR
            synchronized (clientes) {
                for (GameHandler handler : clientes) {
                    handler.setLatch(mainLatch);
                    // O Handler saberá se deve usar Barreira ou não vendo se a sua Equipa tem barreira != null
                    // OU podes setar uma flag no handler: handler.setTeamMode(isTeamRound);
                }
            }

            System.out.println("⏳ À espera de respostas...");
            try {
                mainLatch.await(); // Bloqueia o servidor aqui
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 4 ▬▬▬ CALCULAR E ENVIAR PLACAR DAS EQUIPAS
            System.out.println("📊 A enviar classificações por equipa...");

            ConcurrentHashMap<String, Integer> placarEquipas = new ConcurrentHashMap<>();

            // 🔥 CORREÇÃO AQUI: Calcular pontos com base no GameState (Onde os pontos estão guardados)
            for (Team t : server.getTeams()) {
                int totalEquipa = 0;

                // Soma os pontos de cada membro da equipa, indo buscar ao GameState
                for (Player p : t.getMembers()) {
                    totalEquipa += gameState.getPontuacao(p.getUsername());
                }

                // Guarda Nome da Equipa -> Total Calculado
                placarEquipas.put(t.getNome(), totalEquipa);
            }

            // Envia o Snapshot (HashMap normal)
            server.broadcast(new Mensagem(MessagesEnum.SCORE, placarEquipas.snapshot()));

            esperar(2000);

            if (!gameState.proximaPergunta()) {
                break;
            }
        }

        System.out.println("🏆 Jogo Terminado. A enviar resultados finais...");

        // 1. Recalcular Pontuações Finais (Igual ao que fazes dentro do loop)
        ConcurrentHashMap<String, Integer> placarFinal = new ConcurrentHashMap<>();
        for (Team t : server.getTeams()) {
            int totalEquipa = 0;
            for (Player p : t.getMembers()) {
                totalEquipa += gameState.getPontuacao(p.getUsername());
            }
            placarFinal.put(t.getNome(), totalEquipa);
        }

        // 2. Enviar Mensagem de GAME_OVER com o mapa
        server.broadcast(new Mensagem(MessagesEnum.GAME_OVER, placarFinal.snapshot()));
        esperar(2000);
    }

    // --- Lógica Auxiliar de Pontuação de Equipa ---
    private void calcularPontuacaoEquipa(Team equipa, Question q) {
        List<Player> membros = equipa.getMembers();
        boolean todosAcertaram = true;
        int maxPontosMember = 0;

        // Verifica respostas
        for (Player p : membros) {
            int resposta = p.getLastAnswer(); // Tens de guardar a resposta no Player/Handler
            if (q.isCorrect(resposta)) {
                maxPontosMember = q.getPoints(); // Guarda a pontuação base
            } else {
                todosAcertaram = false;
            }
        }

        int pontosFinais = 0;
        if (todosAcertaram && !membros.isEmpty()) {
            pontosFinais = q.getPoints() * 2; // BÓNUS: Duplica se todos acertarem
            System.out.println("Equipa " + equipa.getNome() + " ACERTOU TUDO! (Dobro)");
        } else {
            pontosFinais = maxPontosMember; // Sem bónus, conta o melhor
            System.out.println("Equipa " + equipa.getNome() + " parcial/falha.");
        }

        // Adiciona pontos a todos os membros (ou à equipa no GameState)
        for (Player p : membros) {
            gameState.adicionarPontos(p.getUsername(), pontosFinais);
        }
    }

    private void esperar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}