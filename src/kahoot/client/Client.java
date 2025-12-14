package kahoot.client;

import kahoot.messages.Mensagem;
import kahoot.messages.MessagesEnum;

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


            if (handler == null) {

            }

            System.out.println(" Ligado ao servidor!");


            String[] dadosLogin = {nomeJogador, "SemEquipa"};
            enviarMensagem(new Mensagem(MessagesEnum.LOGIN, dadosLogin));

            return true;

        } catch (Exception e) {
            System.err.println(" Erro ao ligar ao servidor: " + e.getMessage());
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

    public void receberMensagem(Mensagem msg) {
        System.out.println(" Recebido do servidor: " + msg.getType() + " -> " + msg.getContent());
    }

    public static void main(String[] args) {
        Client c = new Client("Jogador1");
        c.ligar("localhost", 12345);
    }
}