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

    private ServerHandler handler; // Certifica-te que tens esta classe ou usa a thread local

    private String nomeJogador;

    public Client(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public boolean ligar(String servidor, int porta) {
        try {
            socket = new Socket(servidor, porta);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Arranca thread que escuta o servidor (Supondo que tens esta classe ServerHandler)
            // Se não tiveres ServerHandler, terás de criar uma thread aqui como no MainGuiDemo
            if (handler == null) {
                // handler = new ServerHandler(in, this);
                // handler.start();
                // COMENTEI EM CIMA PORQUE NÃO SEI SE TENS A CLASSE ServerHandler FEITA.
                // Se não tiveres, usa a lógica de thread do MainGuiDemo.
            }

            System.out.println("🟢 Ligado ao servidor!");

            // --- CORREÇÃO 1: Enviar LOGIN corretamente ---
            // O servidor espera um array String[] {user, team}
            String[] dadosLogin = {nomeJogador, "SemEquipa"};
            enviarMensagem(new Mensagem(MessagesEnum.LOGIN, dadosLogin));

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
        // --- CORREÇÃO 2: Usar getContent() e não getTexto() ---
        System.out.println("📩 Recebido do servidor: " + msg.getType() + " -> " + msg.getContent());
    }

    public static void main(String[] args) {
        Client c = new Client("Jogador1");
        c.ligar("localhost", 12345);
    }
}