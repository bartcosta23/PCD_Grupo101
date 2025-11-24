package kahoot.client;

import kahoot.messages.Mensagem;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ServerHandler handler;

    private String nomeJogador;

    public Client(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public boolean ligar(String servidor, int porta) {
        try {
            socket = new Socket(servidor, porta);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Arranca thread que escuta o servidor
            handler = new ServerHandler(in, this);
            handler.start();

            System.out.println("🟢 Ligado ao servidor!");
            enviarMensagem(new Mensagem(nomeJogador, "JOIN"));

            return true;

        } catch (Exception e) {
            System.err.println("❌ Erro ao ligar ao servidor: " + e.getMessage());
            return false;
        }
    }

    public void enviarMensagem(Mensagem msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            System.err.println("⚠ Erro ao enviar: " + e.getMessage());
        }
    }

    // Callback quando o servidor envia algo
    public void receberMensagem(Mensagem msg) {
        System.out.println("📩 Recebido do servidor: " + msg.getTexto());
        // Aqui ligamos com a GUI depois
    }
    public static void main(String[] args) {
        Client c = new Client("Jogador1");

        c.ligar("localhost", 5001);
    }

}

