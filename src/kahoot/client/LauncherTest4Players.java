package kahoot.client;

import kahoot.gui.MainGuiDemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LauncherTest4Players {

    // Quantos clientes queres abrir?
    private static final int NUM_CLIENTES = 4;

    public static void main(String[] args) {
        System.out.println("🚀 A lançar " + NUM_CLIENTES + " clientes automaticamente...");

        for (int i = 0; i < NUM_CLIENTES; i++) {
            iniciarNovoCliente();
            try {
                // Pequena pausa para as janelas não abrirem todas encavalitas
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void iniciarNovoCliente() {
        try {
            // 1. Descobrir onde está o java (java.exe ou java bin)
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            // 2. Descobrir o Classpath atual (onde estão as tuas classes compiladas)
            String classpath = System.getProperty("java.class.path");

            // 3. Qual a classe que queremos correr? (A GUI)
            String className = MainGuiDemo.class.getName();

            // 4. Construir o comando: "java -cp ... kahoot.gui.MainGuiDemo"
            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(classpath);
            command.add(className);

            // 5. Lançar o processo
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start(); // 🔥 ISTO CRIA UMA NOVA JVM (Janela independente)

            System.out.println("✅ Cliente lançado!");

        } catch (IOException e) {
            System.err.println("❌ Erro ao lançar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}