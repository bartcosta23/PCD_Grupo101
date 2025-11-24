package kahoot.server;

import kahoot.messages.*;

import java.io.*;
import java.net.Socket;

public class GameHandler extends Thread {

    private Socket socket;
    private GameServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public GameHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            System.out.println("📥 Streams criados para cliente.");

            while (true) {

                Object obj = in.readObject();

                if (obj instanceof Mensagem msg) {
                    System.out.println("📨 Mensagem recebida de " + msg.getAutor());
                    server.broadcast(msg);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Cliente desligou: " + e.getMessage());
        }
    }

    public void enviar(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            System.err.println("❌ Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
