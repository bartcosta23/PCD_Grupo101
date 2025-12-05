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

        // 1. Configurar Equipas
        System.out.print("🔢 Quantas equipas vão jogar? ");
        int numEquipas = 0;
        try {
            numEquipas = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido. A sair.");
            System.exit(0);
        }

        for (int i = 1; i <= numEquipas; i++) {
            System.out.print("📛 Nome da Equipa " + i + ": ");
            String nomeEquipa = scanner.nextLine();

            // Gera um código de 4 caracteres (ex: A1B2)
            String codigo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            Team novaEquipa = new Team(nomeEquipa);
            equipasPorCodigo.put(codigo, novaEquipa);

            System.out.println("✅ Equipa criada! [" + nomeEquipa + "] -> CÓDIGO: " + codigo);
        }

        System.out.println("\n📋 --- TABELA DE CÓDIGOS (PARTILHAR COM ALUNOS) ---");
        for (Map.Entry<String, Team> entry : equipasPorCodigo.entrySet()) {
            System.out.println("🔑 Código: " + entry.getKey() + "  ➡  Equipa: " + entry.getValue().getNome());
        }
        System.out.println("---------------------------------------------------\n");

        System.out.println("🚀 A iniciar servidor...");

        // Passamos o mapa de códigos para o servidor
        new GameServer(equipasPorCodigo).startServer();
    }
}