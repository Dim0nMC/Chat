package chat;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    private static Map<String, Connection> connections = new ConcurrentHashMap<>();

    private static class ServerThread extends Thread {
        private Socket socket;

        ServerThread(Socket socket) {
            this.socket = socket;

        }

        private String serverPrepare(Connection connection) throws IOException, ClassNotFoundException {
            if (Objects.isNull(connection)) return "";
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message msg = connection.receive();
                if (msg.getType() != MessageType.USER_NAME) continue;
                if (Objects.isNull(msg.getData()) || msg.getData().isEmpty()) continue;
                if (connections.containsKey(msg.getData())) continue;
                connections.put(msg.getData(), connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return msg.getData();
            }
        }

        private void notifyUsers(Connection connection, String excludeUserName) throws IOException {
            for (Map.Entry<String, Connection> entry: connections.entrySet()) {
                if (!excludeUserName.equals(entry.getKey())) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }

        private void serverProcess(Connection connection, String userName)
                throws IOException, ClassNotFoundException {
            while (true) {
                try {
                    Message msg = connection.receive();
                    if (msg.getType() == MessageType.TEXT) {
                        Message new_msg = new Message(MessageType.TEXT, userName + ": " + msg.getData());
                        sendMessageToAllUsers(new_msg);
                    } else Console.write("Invalid message type");
                } catch (SocketException e) { //Обработка завершения работы клиентом
                    if (e.getMessage().equals("Connection reset")) {
                        return;
                    }
                }
            }
        }

        @Override
        public void run() {
            Console.write("Установлено соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            String userName = "";
            try (Connection con = new Connection(socket)) {
                userName = serverPrepare(con);
                Server.sendMessageToAllUsers(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(con, userName);
                serverProcess(con, userName);
                Console.write("Соединение с удаленным адресом " + socket.getRemoteSocketAddress() + " закрыто");
            } catch (IOException | ClassNotFoundException e) {
                Console.write("Произошла ошибка при обмене данными с удаленным адресом " + socket.getRemoteSocketAddress());
            }
            if (!userName.isEmpty()) {
                connections.remove(userName);
                Server.sendMessageToAllUsers(new Message(MessageType.USER_REMOVED, userName));
            }
        }

    }

    public static void sendMessageToAllUsers(Message message) {
        for (Map.Entry<String, Connection> entry: connections.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Сообщение пользователю " + entry.getKey() + " не отправлено.");
            }
        }
    }

    public static void main(String[] args) {
        int port = Console.readInt("Введите порт сервера: ");
        try (ServerSocket ss = new ServerSocket(port)) {
            while (true) {
                Socket cs = ss.accept();
                new ServerThread(cs).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
