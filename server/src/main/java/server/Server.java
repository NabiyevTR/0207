package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("server started");

            while (true) {
                socket = server.accept();
                System.out.println("client connected" + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ] : %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String recieverNickName, String msg) {
       List<ClientHandler> clientsByNickName = getConnectedClientsByNickName(recieverNickName);

       if (clientsByNickName.isEmpty()) {
           sender.sendMsg(String.format( "Клиент с именем %s не подключен к сети", recieverNickName));
       } else {
           String message = String.format("[ %s ] : %s", sender.getNickname(), msg);
           sender.sendMsg(message);
           clientsByNickName.forEach(c -> c.sendMsg(message));
       }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }

       private List<ClientHandler>  getConnectedClientsByNickName(String nickName) {
        return clients.stream()
                .filter(c -> c.getNickname().equals(nickName))
                .collect(Collectors.toList());
    }
}
