package kahoot.client;

import kahoot.gui.MainGuiDemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LauncherTest4Players {


    private static final int NUM_CLIENTES = 4;

    public static void main(String[] args) {
        System.out.println(" A lançar " + NUM_CLIENTES + " clientes automaticamente...");

        for (int i = 0; i < NUM_CLIENTES; i++) {
            iniciarNovoCliente();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void iniciarNovoCliente() {
        try {

            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";


            String classpath = System.getProperty("java.class.path");


            String className = MainGuiDemo.class.getName();


            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(classpath);
            command.add(className);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();

            System.out.println(" Cliente lançado!");

        } catch (IOException e) {
            System.err.println(" Erro ao lançar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}