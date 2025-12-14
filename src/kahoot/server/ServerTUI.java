package kahoot.server;

import kahoot.game.Question;
import kahoot.game.QuizLoader;
import kahoot.game.Team;

import java.util.*;

public class ServerTUI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int contadorJogos = 1;




        System.out.println(" A iniciar o Servidor Central...");
        GameServer centralServer = new GameServer();
        centralServer.start(); // Corre em paralelo numa thread separada


        // CARREGAR PERGUNTAS (Apenas uma vez)

        List<Question> perguntasBase = QuizLoader.load("src/quizzes.json");
        if (perguntasBase == null || perguntasBase.isEmpty()) {
            System.out.println(" Erro: Não foi possível carregar 'quizzes.json'. A sair.");
            System.exit(1);
        }
        System.out.println(" Perguntas carregadas: " + perguntasBase.size());


        //  MENU DE GESTÃO

        while (true) {

            System.out.println("   GESTOR DE JOGOS KAHOOT (MULTI) ");

            System.out.println("1.  Criar Novo Jogo (Sala)");
            System.out.println("2.  Sair do Servidor");
            System.out.print(" Opção: ");

            String opcao = scanner.nextLine();

            if (opcao.equals("2")) {
                System.out.println(" A encerrar servidor...");
                centralServer.stopServer(); // Opcional, se implementaste o método stop
                System.exit(0);
            }

            if (opcao.equals("1")) {
                criarNovoJogo(scanner, contadorJogos, perguntasBase);
                contadorJogos++;
            }
        }
    }

    private static void criarNovoJogo(Scanner scanner, int id, List<Question> perguntas) {
        System.out.println("\n---  CONFIGURAR JOGO " + id + " ---");

        int numEquipas = 0;
        try {
            System.out.print(" Quantas equipas vão jogar? ");
            numEquipas = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(" Número inválido.");
            return;
        }

        Map<String, Team> equipasDesteJogo = new HashMap<>();

        for (int i = 1; i <= numEquipas; i++) {
            System.out.print(" Nome da Equipa " + i + ": ");
            String nome = scanner.nextLine();
            if (nome.isBlank()) nome = "Equipa " + i;

            // Gera código aleatório de 4 caracteres
            String codigo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            Team t = new Team(nome);
            equipasDesteJogo.put(codigo, t);

            System.out.println("    Criada: [" + nome + "] -> CÓDIGO: " + codigo);
        }

        // Criar o identificador do jogo
        String idJogo = "JOGO-" + id;

        //  Criar a Sala (GameRoom)
        // Passamos uma cópia das perguntas para garantir independência
        GameRoom novaSala = new GameRoom(idJogo, equipasDesteJogo, new ArrayList<>(perguntas));

        //  Registar a Sala na Receção (GameServer)
        // Isto diz ao servidor central: "Quem vier com o código X, manda para a sala JOGO-Y"
        GameServer.registarNovoJogo(novaSala, equipasDesteJogo);

        System.out.println("\n JOGO CRIADO COM SUCESSO!");
        System.out.println(" Partilha estes códigos com os jogadores:");
        for (var entry : equipasDesteJogo.entrySet()) {
            System.out.println("    Código: " + entry.getKey() + "  ➡  " + entry.getValue().getNome());
        }
        System.out.println("----------------------------------------------");
    }
}