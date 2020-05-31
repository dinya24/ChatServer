import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class Server {
    private final int PORT = 8189;
    private Vector<ClientHandler> clients;
    private ServerSocket server;
    private IAuthService authService;

    public Server() {
        Socket socket = null;
        this.clients = new Vector();

        try {
            this.server = new ServerSocket(8189);
            this.authService = new BaseAuthService();
            this.authService.start();
            System.out.println("Сервер запущен");

            while(true) {
                System.out.println("Сервер ожидает подключение");
                socket = this.server.accept();
                new ClientHandler(socket, this);
                System.out.println("Клиент подключился");
            }
        } catch (IOException var10) {
            var10.printStackTrace();
            System.out.println("Не удалось запустить сервер");
        } finally {
            try {
                this.server.close();
                socket.close();
            } catch (IOException var9) {
                var9.printStackTrace();
            }

            this.authService.stop();
        }

    }

    public synchronized void broadcast(String msg) {
        Iterator var2 = this.clients.iterator();

        while(var2.hasNext()) {
            ClientHandler c = (ClientHandler)var2.next();
            c.sendMessage(msg);
        }

    }

    public synchronized void broadcast(String msg, String... nicks) {
        int countCurrent = 0;
        int countAll = nicks.length;
        Iterator var5 = this.clients.iterator();

        while(var5.hasNext()) {
            ClientHandler c = (ClientHandler)var5.next();
            String[] var7 = nicks;
            int var8 = nicks.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                String nick = var7[var9];
                if (c.getName().equals(nick)) {
                    c.sendMessage(msg);
                    ++countCurrent;
                    if (countCurrent == countAll) {
                        return;
                    }
                }
            }
        }

    }

    public synchronized boolean isNickBusy(String nick) {
        Iterator var2 = this.clients.iterator();

        ClientHandler c;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            c = (ClientHandler)var2.next();
        } while(!c.getName().equals(nick));

        return true;
    }

    public synchronized void subscribe(ClientHandler client) {
        this.clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        this.clients.remove(client);
    }

    public IAuthService getAuthService() {
        return this.authService;
    }

    public void broadcastUserList() {
        StringBuffer sb = new StringBuffer("/user_list");
        ArrayList<String> logins = this.getAuthService().getLoginList();
        Iterator var3 = this.clients.iterator();

        ClientHandler client;
        while(var3.hasNext()) {
            client = (ClientHandler)var3.next();
            sb.append(" " + client.getName() + ":on");
            logins.remove(client.getName());
        }

        var3 = logins.iterator();

        while(var3.hasNext()) {
            String login = (String)var3.next();
            sb.append(" " + login + ":off");
        }

        var3 = this.clients.iterator();

        while(var3.hasNext()) {
            client = (ClientHandler)var3.next();
            client.sendMessage(sb.toString());
        }

    }
}