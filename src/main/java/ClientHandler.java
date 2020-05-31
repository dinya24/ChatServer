
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
            this.name = "";
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        (new Thread(() -> {
            try {
                while(true) {
                    String str = this.in.readUTF();
                    System.out.println("<-Клиент: " + str);
                    String[] elements;
                    String nick;
                    if (str.startsWith("/auth ")) {
                        elements = str.split(" ");
                        if (elements.length != 3) {
                            this.sendMessage("/auth_fail Неверное кол-во параметров");
                            continue;
                        }

                        nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                        if (nick == null) {
                            this.sendMessage("/auth_fail Неверный логин / пароль");
                            continue;
                        }

                        if (server.isNickBusy(nick)) {
                            this.sendMessage("/auth_fail Учётная запись уже используется");
                            continue;
                        }

                        this.sendMessage("/auth_ok " + nick);
                        this.name = nick;
                        this.setAuthorized(true);
                    } else {
                        if (!str.startsWith("/register ")) {
                            this.sendMessage("/auth_fail Для начала нужна авторизация");
                            continue;
                        }

                        elements = str.split(" ");
                        if (elements.length != 3) {
                            this.sendMessage("/register_fail Неверное кол-во параметров");
                            continue;
                        }

                        nick = server.getAuthService().addLoginPass(elements[1], elements[2]);
                        if (nick == null) {
                            this.sendMessage("/register_fail Этот логин уже занят");
                            continue;
                        }

                        this.sendMessage("/register_ok " + nick);
                        this.name = nick;
                        this.setAuthorized(true);
                    }

                    while(true) {
                        str = this.in.readUTF();
                        System.out.println("<-Клиент " + this.name + ": " + str);
                        if (str.equalsIgnoreCase("/end")) {
                            server.broadcast(str, new String[]{this.name});
                            break;
                        }

                        if (str.startsWith("/w ")) {
                            elements = str.split(" ");
                            server.broadcast(this.name + " -> " + elements[1] + " (DM): " + elements[2], new String[]{this.name, elements[1]});
                        } else {
                            if (str.equalsIgnoreCase("/delete")) {
                                server.getAuthService().deleteByLogin(this.name);
                                server.broadcast(str, new String[]{this.name});
                                break;
                            }

                            server.broadcast(this.name + " : " + str);
                        }
                    }

                    this.setAuthorized(false);
                }
            } catch (IOException var13) {
            } finally {
                this.setAuthorized(false);

                try {
                    socket.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                }

            }

        })).start();
    }

    public void sendMessage(String msg) {
        try {
            System.out.println("->Клиент" + (this.name != null ? " " + this.name : "") + ": " + msg);
            this.out.writeUTF(msg);
            this.out.flush();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public String getName() {
        return this.name;
    }

    private void setAuthorized(boolean isAuthorized) {
        if (isAuthorized) {
            this.server.subscribe(this);
            if (!this.name.isEmpty()) {
                this.server.broadcast("Пользователь " + this.name + " зашёл в чат");
                this.server.broadcastUserList();
            }
        } else {
            this.server.unsubscribe(this);
            if (!this.name.isEmpty()) {
                this.server.broadcast("Пользователь " + this.name + " вышел из чата");
                this.server.broadcastUserList();
            }
        }

    }
}
