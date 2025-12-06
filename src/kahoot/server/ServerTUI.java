package kahoot.server;

import kahoot.game.Team;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class ServerTUI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Team> equipasPorCodigo = new HashMap<>();

        System.out.println("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        System.out.println("      SERVIDOR KAHOOT - SETUP     ");
        System.out.println("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        int numEquipas = 0;

        // 🔥 FIXO: O enunciado obriga a ser 2
        int jogadoresPorEquipa = 2;

        try {
            System.out.print("🔢 Quantas equipas vão jogar? ");
            numEquipas = Integer.parseInt(scanner.nextLine());

            if (numEquipas < 1) {
                System.out.println("❌ Tem de haver pelo menos 1 equipa.");
                System.exit(0);
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido.");
            System.exit(0);
        }

        System.out.println("\n--- A criar " + numEquipas + " equipas de 2 jogadores ---\n");

        for (int i = 1; i <= numEquipas; i++) {
            System.out.print("📛 Nome da Equipa " + i + ": ");
            String nomeEquipa = scanner.nextLine();
            if (nomeEquipa.isBlank()) nomeEquipa = "Equipa " + i;

            String codigo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            // Voltar ao construtor simples
            Team novaEquipa = new Team(nomeEquipa);
            equipasPorCodigo.put(codigo, novaEquipa);

            System.out.println("✅ Equipa criada! [" + nomeEquipa + "] -> CÓDIGO: " + codigo);
        }

        System.out.println("\n📋 --- TABELA DE CÓDIGOS ---");
        for (Map.Entry<String, Team> entry : equipasPorCodigo.entrySet()) {
            System.out.println("🔑 Código: " + entry.getKey() + "  ➡  " + entry.getValue().getNome());
        }
        System.out.println("---------------------------------------------------");

        int totalEsperado = numEquipas * 2;
        System.out.println("ℹ️  O jogo começará quando " + totalEsperado + " jogadores entrarem.");
        System.out.println("🚀 A iniciar servidor...");

        // Removemos o argumento extra, o servidor já sabe que são 2
        new GameServer(equipasPorCodigo).startServer();
    }
}