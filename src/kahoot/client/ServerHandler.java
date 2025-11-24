package kahoot.client;

import kahoot.messages.Mensagem;

import java.io.ObjectInputStream;

public class ServerHandler extends Thread {

    private ObjectInputStream in;
    private Client cliente;

    public ServerHandler(ObjectInputStream in, Client cliente) {
        this.in = in;
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Mensagem msg) {
                    cliente.receberMensagem(msg);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Ligação ao servidor perdida: " + e.getMessage());
        }
    }
}
